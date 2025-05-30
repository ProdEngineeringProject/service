package ro.unibuc.hello.dto;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ro.unibuc.hello.data.AuctionEntity;
import ro.unibuc.hello.data.ItemEntity;
import ro.unibuc.hello.data.UserEntity;

public class AuctionWithAuctioneerAndItemTest {

    Item item = new Item("21", "Item 1", "description 1");
    User auctioneer = new User("11", "user 1");
    AuctionWithAuctioneerAndItem auction = new AuctionWithAuctioneerAndItem("1", "Auction 1", "Description 1", 100, "open", auctioneer, item);
    AuctionWithAuctioneerAndItem emptyAuction = new AuctionWithAuctioneerAndItem();
    UserEntity user1 = new UserEntity("11", "user 1", "password1", "username1");
    ItemEntity item1 = new ItemEntity("21", "Item 1", "description 1", user1);
    AuctionWithAuctioneerAndItem auctionFromEntity = new AuctionWithAuctioneerAndItem(new AuctionEntity("1", "Auction 1", "Description 1", 100, true, item1, user1));
    AuctionWithAuctioneerAndItem auctionFromEntity_closed = new AuctionWithAuctioneerAndItem(new AuctionEntity("1", "Auction 1", "Description 1", 100, false, item1, user1));

    @Test
    void testGetId() {
        assertSame("1", auction.getId());
        assertSame("1", auctionFromEntity.getId());
    }

    @Test
    void testGetTitle() {
        assertSame("Auction 1", auction.getTitle());
        assertSame("Auction 1", auctionFromEntity.getTitle());
    }


    @Test
    void testGetDescription() {
        assertSame("Description 1", auction.getDescription());
        assertSame("Description 1", auctionFromEntity.getDescription());
    }

    @Test
    void testGetStartPrice() {
        assertSame(100, auction.getStartPrice());
        assertSame(100, auctionFromEntity.getStartPrice());
    }

    @Test
    void testGetStatus() {
        assertSame("open", auction.getStatus());
        assertSame("open", auctionFromEntity.getStatus());
        assertSame("closed", auctionFromEntity_closed.getStatus());
    }


    @Test
    void testGetAuctioneer() {
        assertEquals("11", auction.getAuctioneer().getId());
        assertEquals("user 1", auction.getAuctioneer().getName());
        assertEquals("11", auctionFromEntity.getAuctioneer().getId());
        assertEquals("user 1", auctionFromEntity.getAuctioneer().getName());
    }


    @Test
    void testGetItem() {
        assertEquals("21", auction.getItem().getId());
        assertEquals("Item 1", auction.getItem().getName());
        assertEquals("description 1", auction.getItem().getDescription());
        assertEquals("21", auctionFromEntity.getItem().getId());
        assertEquals("Item 1", auctionFromEntity.getItem().getName());
        assertEquals("description 1", auctionFromEntity.getItem().getDescription());
    }

    @Test
    void testSetId() {
        emptyAuction.setId("1");
        assertSame("1", emptyAuction.getId());
    }

    @Test
    void testSetTitle() {
        emptyAuction.setTitle("Auction 1");
        assertSame("Auction 1", emptyAuction.getTitle());
    }

    @Test
    void testSetDescription() {
        emptyAuction.setDescription("description 1");
        assertSame("description 1", emptyAuction.getDescription());
    }

    @Test
    void testSetStartPrice() {
        emptyAuction.setStartPrice(20);
        assertSame(20, emptyAuction.getStartPrice());
    }

    @Test
    void testSetStatus() {
        emptyAuction.setStatus("open");
        assertSame("open", emptyAuction.getStatus());
    }

    @Test
    void testSetAuctioneer() {
        User setUser = new User("12", "user 2");
        emptyAuction.setAuctioneer(setUser);

        assertEquals("12", emptyAuction.getAuctioneer().getId());
        assertEquals("user 2", emptyAuction.getAuctioneer().getName());
    }

    @Test
    void testSetItem() {
        Item setItem = new Item("22", "Item 2", "description 2");
        emptyAuction.setItem(setItem);

        assertEquals("22", emptyAuction.getItem().getId());
        assertEquals("Item 2", emptyAuction.getItem().getName());
        assertEquals("description 2", emptyAuction.getItem().getDescription());
    }
}
