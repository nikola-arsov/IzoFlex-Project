package bg.softuni.model.service;

import bg.softuni.model.enumeration.Category;

import java.time.LocalDateTime;

public class ItemServiceModel {
    private String id;
    private String name;
    private Category category;
    private String ownerUsername;
    private String description;
    private boolean forSale;
    private LocalDateTime addedOn;
    private LocalDateTime modifiedOn;

    public ItemServiceModel() {
    }

    public ItemServiceModel(String name, String ownerUsername, boolean forSale) {// constructor used in tests
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.forSale = forSale;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime addedOn) {
        this.addedOn = addedOn;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
}
