package bg.softuni.repository;

import bg.softuni.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> getAllByUser_IdAndSeen(String id, boolean value);
    List<Notification> getAllByUser_UsernameOrderByTimeDesc(String username);
    long countAllByUser_UsernameAndSeen(String username,boolean seen);

}
