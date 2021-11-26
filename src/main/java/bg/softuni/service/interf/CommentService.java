package bg.softuni.service.interf;

import bg.softuni.model.binding.AddCommentModel;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.view.CommentView;
import bg.softuni.model.service.CommentServiceModel;

import java.util.List;

public interface CommentService {
    List<CommentView> getForOffer(String id);

    CommentServiceModel addComment(Offer currentOffer, String authorUsername, AddCommentModel model);

    long removeAllComments(String offerId);

    void removeAllCommentsOlderThanAMonth();
}
