package bg.softuni.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {
    private String content;
    private User author;
    private LocalDateTime postedOn;
    private Offer offer;

    public Comment() {
        this.postedOn=LocalDateTime.now();
    }
    public Comment(Offer offer,User author,String content) {
        this.offer=offer;
        this.author=author;
        this.content=content;
        this.postedOn=LocalDateTime.now();
    }

    @Column(nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne(optional = false)
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Column(name = "posted_on", nullable = false,updatable = false)
    public LocalDateTime getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(LocalDateTime postedOn) {
        this.postedOn = postedOn;
    }

    @ManyToOne(optional = false)
    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }
}
