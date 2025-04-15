package ro.unibuc.hello.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import ro.unibuc.hello.data.SessionEntity;
import ro.unibuc.hello.data.SessionRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class SessionsControllerIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
        .withExposedPorts(27017)
        .withSharding();

    @BeforeAll
    public static void setUp() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        final String MONGO_URL = "mongodb://host.docker.internal:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    public void cleanUpAndAddTestData() {
        userRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void testLogin_Success() throws Exception {
        // Seed data
        UserEntity user = userRepository.save(new UserEntity("user 1", "password1", "username1"));

        // Act & Assert
        mockMvc.perform(post("/session/login")
            .content("{\"username\":\"" + user.getUsername() + "\",\"password\":\"" + user.getPassword() + "\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.user.id").value(user.getId()))
            .andExpect(jsonPath("$.user.name").value(user.getName()));
    }

    @Test
    void testLogin_IncorrectLogin() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/session/login")
            .content("{\"username\":\"username\",\"password\":\"password\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }

    @Test
    void testLogout_Success() throws Exception {
        // Seed data
        UserEntity user = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user, LocalDateTime.now().plusMinutes(1000)));

        // Act & Assert
        mockMvc.perform(post("/session/logout")
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isOk())
            .andExpect(content().string("Logged out successfully."));
    }

    @Test
    void testLogout_NoSessionHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/session/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testLogout_InvalidSession() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/session/logout")
            .header("X-Session-Id", "session3"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid session"));
    }

    @Test
    void testLogout_ExpiredSession() throws Exception {
        // Seed data
        UserEntity user = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user, LocalDateTime.now().minusSeconds(1)));

        // Act & Assert
        mockMvc.perform(post("/session/logout")
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Expired session"));
    }
}
