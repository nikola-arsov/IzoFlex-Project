package bg.softuni.web;

import bg.softuni.model.binding.AddCommentModel;
import bg.softuni.model.view.CommentView;
import bg.softuni.model.view.DetailsView;
import bg.softuni.service.interf.CommentService;
import bg.softuni.util.handler.RedirectHelper;
import bg.softuni.service.interf.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final OfferService offerService;

    @Autowired
    public CommentController(CommentService commentService, OfferService offerService) {
        this.commentService = commentService;
        this.offerService = offerService;
    }

    @GetMapping("/offers/{id}")
    public String getCommentsPageForOffer(@PathVariable("id") String id, Model model) {
        DetailsView data = this.offerService.getDetails(id);

        if (model.getAttribute("formModel") == null) {
            model.addAttribute("formModel", new AddCommentModel());
        }
        model.addAttribute("offer", data);
        model.addAttribute("comments", this.commentService.getForOffer(id));


        return "comments";
    }

    @GetMapping("/offers/{id}/refresh")
    @ResponseBody
    public ResponseEntity<List<CommentView>> refreshComments(@PathVariable("id") String id) {
        return new ResponseEntity<>( this.commentService.getForOffer(id), HttpStatus.OK);
    }

    @PostMapping("/offers/{id}/add")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String commentsAdd(@Valid @ModelAttribute("formModel") AddCommentModel fromModel, BindingResult bindingResult, @PathVariable("id") String offerId, Principal principal, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            this.commentService.addComment(this.offerService.getById(offerId), principal.getName(), fromModel);
        } else {
            RedirectHelper.addAttributes(redirectAttributes, "formModel", fromModel, bindingResult);
        }
        return "redirect:/comments/offers/" + offerId;
    }

    @ExceptionHandler({IllegalStateException.class})
    public void redirectToIllegalArgumentEx(IllegalStateException ex) {
        throw new IllegalArgumentException(ex.getMessage());
    }
}