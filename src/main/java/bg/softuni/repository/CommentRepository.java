package bg.softuni.repository;

import bg.softuni.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> getAllByOffer_IdOrderByPostedOnDesc(String id);
    long deleteAllByOffer_Id(String id);
}
