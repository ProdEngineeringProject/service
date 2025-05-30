package ro.unibuc.hello.dto;

import ro.unibuc.hello.data.ItemEntity;

public class Item {
    private String id;
    private String name;
    private String description;

    public Item() {}

    public Item(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Item(ItemEntity entity) {
        this(
            entity.getId(),
            entity.getName(),
            entity.getDescription()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
