package bg.softuni;

import bg.softuni.model.entity.Notification;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.NotificationServiceModel;
import bg.softuni.model.view.NotificationView;
import bg.softuni.repository.NotificationRepository;
import bg.softuni.service.impl.NotificationServiceImpl;
import bg.softuni.service.interf.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    public void init() {
        this.notificationService = new NotificationServiceImpl(notificationRepository, new ModelMapper());
    }

    @Test
    public void testGetUnreadNotificationsCount() {
        when(notificationRepository.getAllByUser_IdAndSeen(any(), anyBoolean())).thenReturn(List.of(new Notification(new User(), "TEST TEXT")));
        assertEquals(1L, this.notificationService.getUnreadNotificationsCount("some id"));
    }

    @Test
    public void testDoesUserHaveUnseenNotifications() {
        when(notificationRepository.countAllByUser_UsernameAndSeen(anyString(), anyBoolean())).thenReturn(1L);
        assertTrue(this.notificationService.doesUserHaveUnseenNotifications("some id"));
        when(notificationRepository.countAllByUser_UsernameAndSeen(anyString(), anyBoolean())).thenReturn(0L);
        assertFalse(this.notificationService.doesUserHaveUnseenNotifications("some id"));
    }

    @Test
    public void testGetAllNotifications() {
        LocalDateTime time = LocalDateTime.now();
        String resultTime = DateTimeFormatter.ofPattern("HH:mm dd-MMM-yyyy").format(time);

        Notification one = new Notification(new User(), "TEST ONE");
        one.setTime(time);
        Notification two = new Notification(new User(), "TEST TWO");
        two.setTime(time);

        when(notificationRepository.getAllByUser_UsernameOrderByTimeDesc(anyString())).thenReturn(List.of(two, one));

        List<NotificationView> result = this.notificationService.getAllNotifications("some user");

        assertEquals("TEST TWO", result.get(0).getText());
        assertEquals(resultTime, result.get(0).getTime());
        assertEquals("TEST ONE", result.get(1).getText());
        assertEquals(resultTime, result.get(1).getTime());
    }

    @Test
    public void testMarkUnseenAsSeen() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        Notification notificationSeen = new Notification(new User(), "UNSEEN TEST");
        notificationSeen.setSeen(true);

        when(notificationRepository.getAllByUser_UsernameOrderByTimeDesc(anyString())).thenReturn(List.of(new Notification(new User(), "UNSEEN TEST")));
        when(notificationRepository.save(any())).thenReturn(notificationSeen);

        List<NotificationServiceModel> result = this.notificationService.markUnseenAsSeen("some test username");
        verify(this.notificationRepository).save(captor.capture());

        assertEquals(result.get(0).isSeen(), captor.getValue().isSeen());
    }

    @Test
    public void testMarkUnseenAsSeenWithNoUnseen() {
        Notification notificationSeen = new Notification(new User(), "UNSEEN TEST");
        notificationSeen.setSeen(true);

        when(notificationRepository.getAllByUser_UsernameOrderByTimeDesc(anyString())).thenReturn(List.of(notificationSeen));
        assertThrows(IllegalArgumentException.class, () -> this.notificationService.markUnseenAsSeen("some test username"));
    }

    @Test
    public void testMarkUnseenAsSeenWithNoNotifications() {
        when(notificationRepository.getAllByUser_UsernameOrderByTimeDesc(anyString())).thenReturn(new ArrayList<>());
        assertThrows(IllegalArgumentException.class, () -> this.notificationService.markUnseenAsSeen("some test username"));
    }

    @Test
    public void testCreateNotification() {
        User user = new User();
        user.setUsername("TEST");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.saveAndFlush(any())).thenReturn(new Notification());
        this.notificationService.createNotification(user, "TEST CONTENT");
        verify(notificationRepository).saveAndFlush(captor.capture());

        assertEquals("TEST", captor.getValue().getUser().getUsername());
        assertEquals("TEST CONTENT", captor.getValue().getText());
    }

    @Test
    public void testRemoveOldNotifications() {
        LocalDateTime old = LocalDateTime.now().minusHours(25);
        Notification temp = new Notification(new User(), "TEST");
        temp.setSeen(true);
        temp.setTime(old);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.findAll()).thenReturn(List.of(temp));

        notificationService.removeOldNotifications();
        verify(notificationRepository).delete(captor.capture());

        assertEquals("TEST",captor.getValue().getText());
    }
}
