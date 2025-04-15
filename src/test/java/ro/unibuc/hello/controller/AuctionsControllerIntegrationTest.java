package ro.unibuc.hello.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;

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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import ro.unibuc.hello.data.AuctionEntity;
import ro.unibuc.hello.data.AuctionRepository;
import ro.unibuc.hello.data.BidEntity;
import ro.unibuc.hello.data.BidRepository;
import ro.unibuc.hello.data.ItemEntity;
import ro.unibuc.hello.data.ItemRepository;
import ro.unibuc.hello.data.SessionEntity;
import ro.unibuc.hello.data.SessionRepository;
import ro.unibuc.hello.data.UserEntity;
import ro.unibuc.hello.data.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("IntegrationTest")
public class AuctionsControllerIntegrationTest {

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
    private ItemRepository itemRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    public void cleanUpAndAddTestData() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bidRepository.deleteAll();
        auctionRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void testGetAll() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("11", "user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("12", "user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("21", "Item 1", "description 1", user1));
        ItemEntity item2 = itemRepository.save(new ItemEntity("22", "Item 2", "description 2", user2));
        auctionRepository.saveAll(Arrays.asList(
            new AuctionEntity("1", "Title 1", "Description 1", 20, true, item1, user1),
            new AuctionEntity("2", "Title 2", "Description 2", 20, false, item2, user2)
        ));

        // Act & Assert
        mockMvc.perform(get("/auctions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].title").value("Title 1"))
            .andExpect(jsonPath("$[0].description").value("Description 1"))
            .andExpect(jsonPath("$[0].startPrice").value(20))
            .andExpect(jsonPath("$[0].status").value("open"))
            .andExpect(jsonPath("$[0].auctioneer.id").value("11"))
            .andExpect(jsonPath("$[0].auctioneer.name").value("user 1"))
            .andExpect(jsonPath("$[0].item.id").value("21"))
            .andExpect(jsonPath("$[0].item.name").value("Item 1"))
            .andExpect(jsonPath("$[0].item.description").value("description 1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[1].title").value("Title 2"))
            .andExpect(jsonPath("$[1].description").value("Description 2"))
            .andExpect(jsonPath("$[1].startPrice").value(20))
            .andExpect(jsonPath("$[1].status").value("closed"))
            .andExpect(jsonPath("$[1].auctioneer.id").value("12"))
            .andExpect(jsonPath("$[1].auctioneer.name").value("user 2"))
            .andExpect(jsonPath("$[1].item.id").value("22"))
            .andExpect(jsonPath("$[1].item.name").value("Item 2"))
            .andExpect(jsonPath("$[1].item.description").value("description 2"));
    }

    @Test
    void testGetAuctionById() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));

        // Act & Assert
        mockMvc.perform(get("/auctions/" + auction.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(auction.getId()))
            .andExpect(jsonPath("$.title").value(auction.getTitle()))
            .andExpect(jsonPath("$.description").value(auction.getDescription()))
            .andExpect(jsonPath("$.startPrice").value(auction.getStartPrice()))
            .andExpect(jsonPath("$.status").value(auction.isOpen() ? "open" : "closed"))
            .andExpect(jsonPath("$.auctioneer.id").value(user1.getId()))
            .andExpect(jsonPath("$.auctioneer.name").value(user1.getName()))
            .andExpect(jsonPath("$.item.id").value(item1.getId()))
            .andExpect(jsonPath("$.item.name").value(item1.getName()))
            .andExpect(jsonPath("$.item.description").value(item1.getDescription()));
    }

    @Test
    void testGetAuctionById_cascadesException() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auctions/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Auction not found"));
    }

    @Test
    void testGetAuctionHighestBid() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("12", "user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        BidEntity bid1 = bidRepository.save(new BidEntity(20, user2, auction));

        // Act & Assert
        mockMvc.perform(get("/auctions/" + auction.getId() + "/highest-bid"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bid1.getId()))
            .andExpect(jsonPath("$.price").value(bid1.getPrice()))
            .andExpect(jsonPath("$.bidder.id").value(user2.getId()))
            .andExpect(jsonPath("$.bidder.name").value(user2.getName()));
    }

    @Test
    void testGetAuctionHighestBid_NoBids() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));

        // Act & Assert
        mockMvc.perform(get("/auctions/" + auction.getId() + "/highest-bid"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    void testGetAuctionHighestBid_AuctionNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auctions/1/highest-bid"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Auction not found"));
    }

    @Test
    void testGetAuctionBids() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("12", "user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        BidEntity bid1 = bidRepository.save(new BidEntity(20, user1, auction));
        BidEntity bid2 = bidRepository.save(new BidEntity(30, user2, auction));

        // Act & Assert
        mockMvc.perform(get("/auctions/" + auction.getId() + "/bids"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(bid1.getId()))
            .andExpect(jsonPath("$[0].price").value(bid1.getPrice()))
            .andExpect(jsonPath("$[0].bidder.id").value(user1.getId()))
            .andExpect(jsonPath("$[0].bidder.name").value(user1.getName()))
            .andExpect(jsonPath("$[1].id").value(bid2.getId()))
            .andExpect(jsonPath("$[1].price").value(bid2.getPrice()))
            .andExpect(jsonPath("$[1].bidder.id").value(user2.getId()))
            .andExpect(jsonPath("$[1].bidder.name").value(user2.getName()));
    }

    @Test
    void testGetAuctionBids_NoAuction() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/auctions/1/bids"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Auction not found"));
    }

    @Test
    void testCreate() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        sessionRepository.save(new SessionEntity("session1", user1, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/auctions")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"" + item1.getId() + "\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Session-Id", "session1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Title 1"))
            .andExpect(jsonPath("$.description").value("Description 1"))
            .andExpect(jsonPath("$.startPrice").value(10))
            .andExpect(jsonPath("$.status").value("open"))
            .andExpect(jsonPath("$.auctioneer.id").value(user1.getId()))
            .andExpect(jsonPath("$.auctioneer.name").value(user1.getName()))
            .andExpect(jsonPath("$.item.id").value(item1.getId()))
            .andExpect(jsonPath("$.item.name").value(item1.getName()))
            .andExpect(jsonPath("$.item.description").value(item1.getDescription()))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        String id = JsonPath.parse(response).read("$.id");
        mockMvc.perform(get("/auctions/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Title 1"))
            .andExpect(jsonPath("$.description").value("Description 1"))
            .andExpect(jsonPath("$.startPrice").value(10))
            .andExpect(jsonPath("$.status").value("open"))
            .andExpect(jsonPath("$.auctioneer.id").value(user1.getId()))
            .andExpect(jsonPath("$.auctioneer.name").value(user1.getName()))
            .andExpect(jsonPath("$.item.id").value(item1.getId()))
            .andExpect(jsonPath("$.item.name").value(item1.getName()))
            .andExpect(jsonPath("$.item.description").value(item1.getDescription()))
            .andReturn();
    }

    @Test
    void testCreate_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auctions")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testUpdateAuction() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user1, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        mockMvc.perform(put("/auctions/" + auction.getId())
            .content("{\"title\":\"Title updated\",\"description\":\"Description updated\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(auction.getId()))
            .andExpect(jsonPath("$.title").value("Title updated"))
            .andExpect(jsonPath("$.description").value("Description updated"))
            .andExpect(jsonPath("$.startPrice").value(auction.getStartPrice()))
            .andExpect(jsonPath("$.status").value(auction.isOpen() ? "open" : "closed"))
            .andExpect(jsonPath("$.auctioneer.id").value(user1.getId()))
            .andExpect(jsonPath("$.auctioneer.name").value(user1.getName()))
            .andExpect(jsonPath("$.item.id").value(item1.getId()))
            .andExpect(jsonPath("$.item.name").value(item1.getName()))
            .andExpect(jsonPath("$.item.description").value(item1.getDescription()));
    }

    @Test
    void testUpdate_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/auctions/1")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testUpdate_Forbidden() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user2, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        mockMvc.perform(put("/auctions/" + auction.getId())
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("You do not own this auction"));
    }

    @Test
    void testPlaceBid() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user2, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        mockMvc.perform(post("/auctions/" + auction.getId() + "/place-bid")
            .content("{\"price\":10}")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(10))
            .andExpect(jsonPath("$.bidder.id").value(user2.getId()))
            .andExpect(jsonPath("$.bidder.name").value(user2.getName()));

        mockMvc.perform(get("/auctions/" + auction.getId() + "/bids"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].price").value(10))
            .andExpect(jsonPath("$[0].bidder.id").value(user2.getId()));
    }

    @Test
    void testPlaceBid_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auctions/1/place-bid")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testCloseAuction() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        bidRepository.save(new BidEntity(20, user2, auction));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user1, LocalDateTime.now().plusMinutes(100)));

        // Act
        mockMvc.perform(post("/auctions/" + auction.getId() + "/close")
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isOk());

        // Assert
        mockMvc.perform(get("/auctions/" + auction.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("closed"));

        mockMvc.perform(get("/items/" + item1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.id").value(user2.getId()));
    }

    @Test
    void testCloseAuction_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/auctions/1/close")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testCloseAuction_Forbidden() throws Exception {
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user2, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        mockMvc.perform(post("/auctions/" + auction.getId() + "/close")
            .content("{\"title\":\"Title 1\",\"description\":\"Description 1\",\"startPrice\":10, \"itemId\":\"21\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("You do not own this auction"));
    }

    @Test
    void testDeleteAuction() throws Exception {
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user1, LocalDateTime.now().plusMinutes(100)));

        // Act
        mockMvc.perform(delete("/auctions/" + auction.getId())
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isOk());

        // Assert
        mockMvc.perform(get("/auctions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDeleteAuction_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/auctions/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Missing session id"));
    }

    @Test
    void testDeleteAuction_Forbidden() throws Exception {
        // Seed data
        UserEntity user1 = userRepository.save(new UserEntity("user 1", "password1", "username1"));
        UserEntity user2 = userRepository.save(new UserEntity("user 2", "password2", "username2"));
        ItemEntity item1 = itemRepository.save(new ItemEntity("Item 1", "description 1", user1));
        AuctionEntity auction = auctionRepository.save(new AuctionEntity("Title 1", "Description 1", 10, true, item1, user1));
        SessionEntity session = sessionRepository.save(new SessionEntity("session1", user2, LocalDateTime.now().plusMinutes(100)));

        // Act & Assert
        mockMvc.perform(delete("/auctions/" + auction.getId())
            .header("X-Session-Id", session.getSessionId()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("You do not own this auction"));
    }
}
