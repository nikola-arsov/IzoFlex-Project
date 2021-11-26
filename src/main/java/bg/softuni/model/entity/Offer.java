package bg.softuni.model.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
public class Offer extends BaseEntity {
    private User seller;
    private Item item;
    private BigDecimal price;
    private LocalDateTime addedOn;

    public Offer() {
        this.addedOn = LocalDateTime.now();
    }

    public Offer(Item item, User owner, BigDecimal price) {
        this.item = item;
        this.seller = owner;
        this.price = price;
        this.addedOn = LocalDateTime.now();
    }

    @ManyToOne(optional = false)
    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    @Column(nullable = false)
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column(name = "added_on", nullable = false, updatable = false)
    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime addedOn) {
        this.addedOn = addedOn;
    }

    @OneToOne(optional = false)
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}