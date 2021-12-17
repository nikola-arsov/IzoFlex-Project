package bg.softuni.web;

import bg.softuni.service.interf.NotificationService;
import bg.softuni.service.interf.OfferService;
import bg.softuni.service.interf.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
    private final UserService userService;
    private final OfferService offerService;
    private final NotificationService notificationService;

    @Autowired
    public HomeController(UserService userService, OfferService offerService, NotificationService notificationService) {
        this.userService = userService;
        this.offerService = offerService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String getHome(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("fullName", this.userService.getFullName(principal.getName()));
            model.addAttribute("itemCount", this.userService.getCollectionCount(principal.getName()));
            model.addAttribute("offerCount", this.offerService.getActiveOffers());
            model.addAttribute("balance", String.format("Вашият баланс по сметката е %s лв.", this.userService.getBalance(principal.getName())));
            model.addAttribute("notificationCount", this.notificationService.getUnreadNotificationsCount(this.userService.getIdByUsername(principal.getName())));
        }
        return "index";
    }
}