package bg.softuni.web;

import bg.softuni.service.interf.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String getNotificationsPage(Principal principal, Model model) {
        model.addAttribute("notifications", this.notificationService.getAllNotifications(principal.getName()));
        model.addAttribute("hasUnseen", this.notificationService.doesUserHaveUnseenNotifications(principal.getName()));
        this.notificationService.removeOldNotifications();
        return "notifications";
    }

    @GetMapping("/mark")
    @PreAuthorize("hasRole('USER')")
    public String markAllAsSeen(Principal principal, RedirectAttributes redirectAttributes) {
        this.notificationService.markUnseenAsSeen(principal.getName());
        redirectAttributes.addFlashAttribute("success", "Прегледахте успешно всички нотификации!");

        return "redirect:/";
    }
}