package bg.softuni.service.interf;

import bg.softuni.model.entity.Offer;
import bg.softuni.model.service.OfferServiceModel;
import bg.softuni.model.view.DetailsView;
import bg.softuni.model.view.OfferView;
import bg.softuni.model.service.ItemServiceModel;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface OfferService {
    long getActiveOffers();

    OfferServiceModel addOffer(String username, String itemId, BigDecimal price);

    Page<OfferView> getOffers(int page);
    Page<OfferView> getOffersByCategory(int page,String category);

    DetailsView getDetails(String id);

    List<OfferView> getMyOffers(String username);

    void removeOffer(String username, String offerId);

    ItemServiceModel buyItem(String username, String offerId);

    Offer getById(String id);
}
