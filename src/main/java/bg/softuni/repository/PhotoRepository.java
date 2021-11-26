package bg.softuni.repository;

import bg.softuni.model.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    Optional<Photo> getFirstByItem_Id(String itemId);
}
