package bg.softuni.web;

import bg.softuni.model.view.OfferView;
import bg.softuni.service.interf.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/offers")
public class OfferController {
    private final OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }


    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public String addOfferPost(@RequestParam(name = "price", defaultValue = "0") BigDecimal price, @RequestParam("itemId") String itemId, Principal principal, RedirectAttributes redirectAttributes) {
        this.offerService.addOffer(principal.getName(), itemId, price);
        redirectAttributes.addFlashAttribute("success", "Успешнo добавихте оферта!");

        return "redirect:/users/collection";
    }

    @GetMapping
    public String getOffersPage(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Page<OfferView> pageable = this.offerService.getOffers(page);
        model.addAttribute("offers", pageable);
        model.addAttribute("max_index", (pageable.getTotalPages() - 1));
        model.addAttribute("current", pageable.getNumber());

        return "offers";
    }

    @GetMapping("/categories/{category}/view")
    public String getBaseHtmlForJSLoading(@RequestParam(name = "page", defaultValue = "0") int page, @PathVariable("category") String category, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("magicLink", String.format("/offers/categories/%s", category));
        redirectAttributes.addFlashAttribute("magicPage", page);

        return "redirect:/offers";
    }

    @ResponseBody
    @GetMapping("/categories/{category}")
    public ResponseEntity<Page<OfferView>> getOffersPerCategory(@PathVariable("category") String category, @RequestParam(name = "page", defaultValue = "0") int page) {
        return new ResponseEntity<>(this.offerService.getOffersByCategory(page, category), HttpStatus.OK);
    }

    @GetMapping("/details/{id}")
    public String getDetailsPage(@PathVariable("id") String id, Model model) {
        model.addAttribute("item", this.offerService.getDetails(id));
        return "details";
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public String getMyOffersPage(Model model, Principal principal) {
        model.addAttribute("offers", this.offerService.getMyOffers(principal.getName()));
        return "my-offers";
    }

    @GetMapping("/remove/{id}")
    @PreAuthorize("hasRole('USER')")
    public String removeOffer(@PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        this.offerService.removeOffer(principal.getName(), id);
        redirectAttributes.addFlashAttribute("success", "Офертата е премахната успешно!");

        return "redirect:/users/collection";
    }

    @PostMapping("/buy/{id}")
    @PreAuthorize("hasRole('USER')")
    public String buyItem(@PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            this.offerService.buyItem(principal.getName(), id);
            redirectAttributes.addFlashAttribute("success", "Успешно закупихте продукта!");

            return "redirect:/users/collection";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/offers/details/" + id;
    }
}