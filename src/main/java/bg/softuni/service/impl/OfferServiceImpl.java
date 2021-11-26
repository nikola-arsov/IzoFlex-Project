package bg.softuni.service.impl;

import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.User;
import bg.softuni.model.service.OfferServiceModel;
import bg.softuni.model.view.DetailsView;
import bg.softuni.model.view.OfferView;
import bg.softuni.service.interf.CommentService;
import bg.softuni.service.interf.PhotoService;
import bg.softuni.util.handler.EnumValidator;
import bg.softuni.model.enumeration.Category;
import bg.softuni.model.service.ItemServiceModel;
import bg.softuni.repository.OfferRepository;
import bg.softuni.service.interf.ItemService;
import bg.softuni.service.interf.OfferService;
import bg.softuni.util.interceptor.OfferViewCounter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {
    private final OfferViewCounter counterInterceptor;
    private final CommentService commentService;
    private final OfferRepository repository;
    private final PhotoService photoService;
    private final ItemService itemService;
    private final ModelMapper modelMapper;
    private final Sort SORT_DATA;
    private final int PAGE_SIZE;

    @Autowired
    public OfferServiceImpl(OfferViewCounter counterInterceptor, CommentService commentService, OfferRepository repository, PhotoService photoService, ItemService itemService, ModelMapper modelMapper) {
        this.counterInterceptor = counterInterceptor;
        this.commentService = commentService;
        this.repository = repository;
        this.photoService = photoService;
        this.itemService = itemService;
        this.modelMapper = modelMapper;
        this.SORT_DATA = Sort.by(Sort.Direction.DESC, "addedOn");
        this.PAGE_SIZE = 5;
    }

    @Override
    public long getActiveOffers() {
        return this.repository.count();
    }

    @Override
    public OfferServiceModel addOffer(String username, String itemId, BigDecimal price) {
        if (price.compareTo(BigDecimal.ONE) < 0) throw new IllegalArgumentException("Price must be at least 1 euro!");
        Item item = this.itemService.validateOwnerAndItemId(itemId, username, "sell it");
        this.itemService.toggleForSale(item);
        User seller = item.getOwner();

        return this.modelMapper.map(this.repository.saveAndFlush(new Offer(item, seller, price)), OfferServiceModel.class);
    }

    @Override
    public DetailsView getDetails(String id) {
        Offer offer = this.repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found!"));

        DetailsView view = this.modelMapper.map(offer, DetailsView.class);
        view.setAddedOn(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(offer.getAddedOn()));
        view.setItemPhotos(offer.getItem().getPhotos().stream().map(Photo::getLocation).collect(Collectors.toList()));

        return view;
    }

    @Override
    public List<OfferView> getMyOffers(String username) {
        return this.repository
                .getAllBySeller_UsernameOrderByAddedOnDesc(username).stream()
                .map(e -> {
                    OfferView view = this.modelMapper.map(e, OfferView.class);
                    view.setImageLocation(this.photoService.getPhotoForThumbnail(e.getItem().getId()));

                    return view;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeOffer(String username, String offerId) {
        Offer offer = this.validateOfferId(offerId);
        if (!offer.getItem().getOwner().getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not the author of the offer, you can't remove it!");
        }
        this.itemService.toggleForSale(offer.getItem());
        this.deleteCommentsAndViewsData(offer);
    }

    @Override
    public ItemServiceModel buyItem(String username, String offerId) {
        Offer offer = this.validateOfferId(offerId);
        if (offer.getItem().getOwner().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can't buy your own item!");
        }

        ItemServiceModel out = this.itemService.buyItem(offer, username);
        this.deleteCommentsAndViewsData(offer);

        return out;
    }

    @Override
    public Offer getById(String id) {
        return this.repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Offer with that id doesn't exist!"));
    }

    private Offer validateOfferId(String offerId) {
        return this.repository.findById(offerId).orElseThrow(() -> new IllegalArgumentException(
                "Offer not found. It can be taken down by the owner,item can be bought, or you entered an invalid id!"));
    }

    private void deleteCommentsAndViewsData(Offer offer) {
        this.counterInterceptor.deleteEntry("/offers/details/" + offer.getId());
        this.commentService.removeAllComments(offer.getId());
        this.repository.delete(offer);
    }

    //Integration
    @Override
    public Page<OfferView> getOffers(int page) {
        Page<Offer> tempPage = this.repository.findAll(PageRequest.of(page, this.PAGE_SIZE, this.SORT_DATA));
        return this.getPage(tempPage, page, null);
    }

    //Integration
    @Override
    public Page<OfferView> getOffersByCategory(int page, String category) {
        EnumValidator.validateEnum(category, Category.class, "args");
        Category catValue = Category.valueOf(category.toUpperCase());
        Page<Offer> currPage = this.repository.getAllByItem_Category(
                catValue, PageRequest.of(page, this.PAGE_SIZE, this.SORT_DATA));

        return this.getPage(currPage, page, catValue);
    }

    //Integration
    private Page<OfferView> getPage(Page<Offer> temp, int page, Category category) {
        if (temp.getTotalPages() - 1 < page && page != 0) {
            if (category != null) {
                temp = this.repository.getAllByItem_Category(category, PageRequest.of(0, this.PAGE_SIZE, this.SORT_DATA));
            } else {
                temp = this.repository.findAll(PageRequest.of(0, this.PAGE_SIZE, this.SORT_DATA));
            }
        }
        return temp.map(e -> {
            OfferView offer = this.modelMapper.map(e, OfferView.class);
            offer.setImageLocation(this.photoService.getPhotoForThumbnail(e.getItem().getId()));

            return offer;
        });
    }
}