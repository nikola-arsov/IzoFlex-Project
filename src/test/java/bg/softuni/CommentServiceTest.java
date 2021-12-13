package bg.softuni;

import bg.softuni.model.binding.AddCommentModel;
import bg.softuni.model.entity.Comment;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.CommentServiceModel;
import bg.softuni.model.view.CommentView;
import bg.softuni.repository.CommentRepository;
import bg.softuni.service.impl.CommentServiceImpl;
import bg.softuni.service.interf.CommentService;
import bg.softuni.service.interf.UserService;
import bg.softuni.util.core.impl.ValidatorUtilImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;

    private CommentService commentService;

    @BeforeEach
    public void init() {
        this.commentService = new CommentServiceImpl(commentRepository, new ModelMapper(), new ValidatorUtilImpl(), userService);
    }

    @Test
    public void testRemoveAllCommentsOlderThanAMonth() {
        ArgumentCaptor<Comment> argumentCaptor = ArgumentCaptor.forClass(Comment.class);
        LocalDateTime oldCommentDate = LocalDateTime.now().minusDays(30);
        Comment comment = new Comment();
        comment.setContent("TEST");
        comment.setPostedOn(oldCommentDate);

        when(commentRepository.findAll()).thenReturn(List.of(comment));
        commentService.removeAllCommentsOlderThanAMonth();
        verify(commentRepository).delete(argumentCaptor.capture());

        assertEquals("TEST", argumentCaptor.getValue().getContent());
    }

    @Test
    public void testRemoveAllComments() {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(commentRepository.deleteAllByOffer_Id(anyString())).thenReturn(1L);
        long value = commentService.removeAllComments("test");
        verify(commentRepository).deleteAllByOffer_Id(argumentCaptor.capture());

        assertEquals("test", argumentCaptor.getValue());
        assertEquals(1L, value);
    }

    @Test
    public void testAddComment() {
        User user = new User();
        user.setUsername("test");

        Comment saved = new Comment();
        saved.setContent("test");
        saved.setAuthor(user);

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        when(commentRepository.saveAndFlush(any())).thenReturn(saved);

        CommentServiceModel result = this.commentService.addComment(new Offer(), "test", new AddCommentModel("test"));

        assertEquals("test", result.getAuthorUsername());
        assertEquals("test", result.getContent());
    }

    @Test
    public void testAddCommentException() {
        User user = new User();
        user.setUsername("test");

        when(userService.getUserByUsername(anyString())).thenReturn(user);
        assertThrows(IllegalStateException.class, () -> commentService.addComment(new Offer(), "test", new AddCommentModel("")));
    }

    @Test
    public void testGetForOffer() {
        Comment one = new Comment();
        one.setContent("test-one");
        one.setPostedOn(LocalDateTime.now());

        Comment two = new Comment();
        two.setContent("test-two");
        two.setPostedOn(LocalDateTime.now());

        when(commentRepository.getAllByOffer_IdOrderByPostedOnDesc(anyString())).thenReturn(List.of(two, one));
        List<CommentView> result = commentService.getForOffer("test");

        assertEquals("test-two", result.get(0).getContent());
        assertEquals("test-one", result.get(1).getContent());
    }
}