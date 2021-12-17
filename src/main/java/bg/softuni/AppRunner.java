package bg.softuni;

import bg.softuni.model.entity.Notification;
import bg.softuni.model.entity.User;
import bg.softuni.model.enumeration.Gender;
import bg.softuni.repository.NotificationRepository;
import bg.softuni.repository.RoleRepository;
import bg.softuni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public AppRunner(PasswordEncoder encoder, NotificationRepository notificationRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.encoder = encoder;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {//Initialize the application with some default users
        if (userRepository.count() == 0) {
            User admin = this.userRepository.save(
                    createBasicUser("admin", "Admin", "Administrator"));
            this.createWelcomeNotification(admin);

            User user = this.userRepository.save(
                    createBasicUser("user", "Nikola", "Arsov"));
            this.createWelcomeNotification(user);
        }
    }

    private User createBasicUser(String type, String firstName, String lastName) {
        User user = new User();

        user.setUsername(type);
        user.setPassword(encoder.encode(type+type));
        user.setEmail(type + "@abv.com");
        user.setBalance(BigDecimal.ZERO);
        user.setCreatedOn(LocalDateTime.now());
        user.setGender(Gender.МЪЖ);
        user.setProfilePicture("/img/male-avatar.svg");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(new HashSet<>("admin".equals(type)
                ? this.roleRepository.findAll()
                : List.of(this.roleRepository.getByName("ROLE_USER"))));

        return user;
    }

    private Notification createWelcomeNotification(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setText(String.format("Здравейте %s %s, добре дошли в нашия сайт!", user.getFirstName(), user.getLastName()));

        return this.notificationRepository.save(notification);
    }
}