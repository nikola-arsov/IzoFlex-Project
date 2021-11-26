package bg.softuni.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "photos")
public class Photo extends BaseEntity {
    private String location;
    private Item item;

    public Photo() {
        this.location="no picture";
    }

    public Photo(String location, Item item) {
        this.location = location;
        this.item = item;
    }


    @Column(nullable = false)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @ManyToOne(optional = false ,fetch = FetchType.LAZY)
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
