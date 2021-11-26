package bg.softuni.model.entity;

import bg.softuni.model.enumeration.Category;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static javax.persistence.CascadeType.*;

@Entity
@Table(name = "items")
public class Item extends BaseEntity {
    private String name;
    private Category category;
    private User owner;
    private String description;
    private boolean forSale;
    private LocalDateTime addedOn;
    private LocalDateTime modifiedOn;
    private Set<Photo> photos;

    public Item() {
        this.addedOn = LocalDateTime.now();
        this.modifiedOn = LocalDateTime.now();
        this.photos = new LinkedHashSet<>();
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Enumerated
    @Column(nullable = false)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.setModifiedOn(LocalDateTime.now());
    }

    @ManyToOne(optional = false)
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "is_for_sale", nullable = false)
    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
        this.setModifiedOn(LocalDateTime.now());
    }

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, cascade = ALL)
    public Set<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(Set<Photo> photos) {
        this.photos = photos;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(nullable = false, columnDefinition = "TEXT")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.setModifiedOn(LocalDateTime.now());
    }

    @Column(name = "added_on", nullable = false, updatable = false)
    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime addedOn) {
        this.addedOn = addedOn;
    }

    @Column(name = "modified_on", nullable = false)
    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
}
