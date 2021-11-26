package bg.softuni.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    private String text;
    private User user;
    private boolean seen;
    private LocalDateTime time;

    public Notification() {
        this.time = LocalDateTime.now();
    }

    public Notification(User user, String text) {
        this.time = LocalDateTime.now();
        this.user = user;
        this.text = text;
    }

    @Column(nullable = false)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @ManyToOne(optional = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(nullable = false)
    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
