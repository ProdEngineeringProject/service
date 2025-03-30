package ro.unibuc.hello.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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

import ro.unibuc.hello.data.ItemEntity;
import ro.unibuc.hello.data.SessionEntity;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.ItemRepository;
import ro.unibuc.hello.data.SessionRepository;
import ro.unibuc.hello.data.UserRepository;
import ro.unibuc.hello.service.ItemsService;
import ro.unibuc.hello.dto.ItemPostRequest;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class ItemsControllerIntegrationTest {

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
        final String MONGO_URL = "mongodb://localhost:";
        final String PORT = String.valueOf(mongoDBContainer.getMappedPort(27017));

        registry.add("mongodb.connection.url", () -> MONGO_URL + PORT);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemsService itemsService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    void cleanUpAndAddTestData() {
        itemsService.deleteAllItems();
        userRepository.deleteAll();
        sessionRepository.deleteAll();

        UserEntity user = new UserEntity("1", "Test User", "testuser", "password");
        user = userRepository.save(user);

        SessionEntity session = new SessionEntity("TestSessionId", user, LocalDateTime.now().plusMinutes(5000));
        session = sessionRepository.save(session);

        ItemEntity item1 = new ItemEntity("1", "Item1", "Description1", user);
        item1 = itemRepository.save(item1);
        ItemEntity item2 = new ItemEntity("2", "Item2", "Description2", user);
        item2 = itemRepository.save(item2);
    }

    @Test
    public void testGetAllItems() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Item1"))
                .andExpect(jsonPath("$[1].name").value("Item2"));
    }

    @Test
    public void testGetItemById() throws Exception {
        mockMvc.perform(get("/items/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2"))
                .andExpect(jsonPath("$.name").value("Item2"));
    }

    @Test
    public void testCreateItem() throws Exception {
        ItemPostRequest request = new ItemPostRequest("New Item", "New Description");

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .header("X-Session-Id", "TestSessionId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Item"));
        
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    public void testUpdateItem() throws Exception {
        ItemPostRequest request = new ItemPostRequest("Updated Item", "Updated Description");

        mockMvc.perform(put("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .header("X-Session-Id", "TestSessionId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    public void testDeleteItem() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Session-Id", "TestSessionId"))
                .andExpect(status().isOk());
        //checks if the deleted item still exists  
        Optional<ItemEntity> deletedItem = itemRepository.findById("1");
        Assertions.assertTrue(deletedItem.isEmpty());
    }

}