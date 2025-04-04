package ro.unibuc.hello.dto;

import ro.unibuc.hello.data.AuctionEntity;

public class Auction {

    private String id;
    private String title;
    private String description;
    private int startPrice;
    private String status;

    public Auction() {}

    public Auction(String id, String title, String description, int startPrice, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.status = status;
    }

    public Auction(AuctionEntity entity) {
        this(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getStartPrice(),
            entity.isOpen() ? "open" : "closed"
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
