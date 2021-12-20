package bg.softuni.service.impl;

import bg.softuni.model.entity.Notification;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.NotificationServiceModel;
import bg.softuni.model.view.NotificationView;
import bg.softuni.repository.NotificationRepository;
import bg.softuni.service.interf.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final ModelMapper modelMapper;

    @Autowired
    public NotificationServiceImpl(NotificationRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public long getUnreadNotificationsCount(String id) {
        return this.repository.getAllByUser_IdAndSeen(id, false).size();
    }

    @Override
    public boolean doesUserHaveUnseenNotifications(String username) {
        return this.repository.countAllByUser_UsernameAndSeen(username, false) > 0;
    }

    @Override
    public List<NotificationView> getAllNotifications(String username) {
        return this.repository
                .getAllByUser_UsernameOrderByTimeDesc(username).stream()
                .map(e -> {
                    NotificationView view = this.modelMapper.map(e, NotificationView.class);
                    view.setTime(DateTimeFormatter.ofPattern("HH:mm dd-MMM-yyyy").format(LocalDateTime.now()));

                    return view;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationServiceModel> markUnseenAsSeen(String username) {
        List<Notification> all = this.repository.getAllByUser_UsernameOrderByTimeDesc(username);
        List<NotificationServiceModel> output = new ArrayList<>();

        all.forEach(n -> {
            if (!n.isSeen()) {
                n.setSeen(true);
                output.add(this.modelMapper.map(this.repository.save(n), NotificationServiceModel.class));
            }
        });
        this.checkIfUserExistsAndHasNotifications(all, output);

        return output;
    }

    @Override
    public NotificationServiceModel createNotification(User user, String text) {
        Notification temp = new Notification(user, text);

        return this.modelMapper.map(this.repository.saveAndFlush(temp), NotificationServiceModel.class);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Execute in every midnight
    public void removeOldNotifications() {
        this.repository.findAll().forEach(e -> {
            long diff = Duration.between(e.getTime(), LocalDateTime.now()).toHours();

            if (diff >= 24 && e.isSeen()) {
                this.repository.delete(e);
            }
        });
    }

    private void checkIfUserExistsAndHasNotifications(List<Notification> all, List<NotificationServiceModel> output) {
        if (all.size() == 0) {
            throw new IllegalArgumentException("That user doesn't have any notifications or doesn't exist!");
        }
        if (output.size() == 0) {
            throw new IllegalArgumentException("You don't have any unseen notifications.");
        }
    }
}