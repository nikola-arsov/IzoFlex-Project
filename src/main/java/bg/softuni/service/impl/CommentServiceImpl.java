package bg.softuni.service.impl;

import bg.softuni.model.binding.AddCommentModel;
import bg.softuni.model.entity.Comment;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.User;
import bg.softuni.model.view.CommentView;
import bg.softuni.util.core.ValidatorUtil;
import bg.softuni.model.service.CommentServiceModel;
import bg.softuni.repository.CommentRepository;
import bg.softuni.service.interf.CommentService;
import bg.softuni.service.interf.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final ModelMapper modelMapper;
    private final ValidatorUtil validator;
    private final UserService userService;

    @Autowired
    public CommentServiceImpl(CommentRepository repository, ModelMapper modelMapper, ValidatorUtil validator, UserService userService) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.validator = validator;
        this.userService = userService;
    }

    @Override
    public List<CommentView> getForOffer(String id) {
        return this.repository.getAllByOffer_IdOrderByPostedOnDesc(id).stream()
                .map(e -> {
                    CommentView view = this.modelMapper.map(e, CommentView.class);
                    view.setTime(DateTimeFormatter.ofPattern("HH:mm dd-MMM-yyyy").format(e.getPostedOn()));

                    return view;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentServiceModel addComment(Offer currentOffer, String authorUsername, AddCommentModel model) {
        User user = this.userService.getUserByUsername(authorUsername);

        if (!this.validator.isValid(model)) {
            throw new IllegalStateException("Binding model has errors,check it again and resubmit later!");
        }
        Comment comment = new Comment(currentOffer, user, model.getContent());

        return this.modelMapper.map(this.repository.saveAndFlush(comment), CommentServiceModel.class);
    }

    @Override
    @Transactional
    public long removeAllComments(String offerId) {
        return this.repository.deleteAllByOffer_Id(offerId);
    }

    @Override
    @Scheduled(cron = "0 0 12 15 * ?") //Execute at 12:15 PM (noon) every day
    public void removeAllCommentsOlderThanAMonth() {
        this.repository.findAll().forEach(e -> {
            long diff = Duration.between(e.getPostedOn(), LocalDateTime.now()).toDays();

            if (diff >= 28) {
                this.repository.delete(e);
            }
        });
    }
}