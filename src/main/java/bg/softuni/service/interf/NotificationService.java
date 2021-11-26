package bg.softuni.service.interf;

import bg.softuni.model.entity.User;
import bg.softuni.model.view.NotificationView;
import bg.softuni.model.service.NotificationServiceModel;

import java.util.List;

public interface NotificationService {
    long getUnreadNotificationsCount(String id);

    boolean doesUserHaveUnseenNotifications(String username);

    List<NotificationView> getAllNotifications(String username);

    List<NotificationServiceModel> markUnseenAsSeen(String username);

    NotificationServiceModel createNotification(User user, String text);

    void removeOldNotifications();
}