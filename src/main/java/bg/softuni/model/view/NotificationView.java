package bg.softuni.model.view;

import java.time.LocalDateTime;

public class NotificationView {
    private String text;
    private boolean seen;
    private String time;

    public NotificationView() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
