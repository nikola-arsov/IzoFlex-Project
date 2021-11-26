package bg.softuni.repository;

import bg.softuni.model.entity.Offer;
import bg.softuni.model.enumeration.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {
    List<Offer> getAllBySeller_UsernameOrderByAddedOnDesc(String username);

    Page<Offer> getAllByItem_Category(Category category, Pageable pageable);
}
