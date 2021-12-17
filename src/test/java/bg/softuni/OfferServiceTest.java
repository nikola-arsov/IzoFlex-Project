package bg.softuni;

import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.User;
import bg.softuni.model.view.DetailsView;
import bg.softuni.model.view.OfferView;
import bg.softuni.repository.OfferRepository;
import bg.softuni.service.impl.OfferServiceImpl;
import bg.softuni.service.interf.CommentService;
import bg.softuni.service.interf.ItemService;
import bg.softuni.service.interf.PhotoService;
import bg.softuni.model.service.ItemServiceModel;
import bg.softuni.service.interf.OfferService;
import bg.softuni.util.interceptor.OfferViewCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OfferServiceTest {
    @Mock
    private OfferRepository repository;
    @Mock
    private PhotoService photoService;
    @Mock
    private CommentService commentService;
    @Mock
    private ItemService itemService;
    private OfferService offerService;
    private User owner;
    private Item temp;
    private Offer offer;
    private LocalDateTime time;


    @BeforeEach
    public void init() {
        this.offerService = new OfferServiceImpl(new OfferViewCounter(), commentService, repository, photoService, itemService, new ModelMapper());
        this.owner = new User();
        this.owner.setUsername("TEST");
        this.temp = new Item();
        this.temp.setOwner(this.owner);
        this.temp.setName("ITEM");
        this.temp.setForSale(true);
        this.offer = new Offer(this.temp, this.owner, BigDecimal.TEN);
        this.time = LocalDateTime.now();
        this.offer.setAddedOn(time);
        this.offer.setId("ID");
    }

    @Test
    public void testGetActiveOffers() {
        when(repository.count()).thenReturn(2L);
        assertEquals(2L, offerService.getActiveOffers());
    }

    @Test
    public void testAddOffer() {
        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        when(itemService.validateOwnerAndItemId(anyString(), anyString(), anyString())).thenReturn(this.temp);
        when(repository.saveAndFlush(any())).thenReturn(new Offer());
        offerService.addOffer("TEST", "some id", BigDecimal.TEN);
        verify(repository).saveAndFlush(captor.capture());

        assertEquals("TEST", captor.getValue().getSeller().getUsername());
        assertEquals("ITEM", captor.getValue().getItem().getName());
        assertTrue(captor.getValue().getItem().isForSale());
    }

    @Test
    public void testAddOfferPriceError() {
        assertThrows(IllegalArgumentException.class, () -> offerService.addOffer("TEST", "some id", BigDecimal.valueOf(-50L)));
    }

    @Test
    public void testGetDetailsWithWrongId() {
        when(this.repository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> offerService.getDetails("TEST"));
    }

    @Test
    public void testGetDetails() {
        String format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(this.time);
        when(this.repository.findById(anyString())).thenReturn(Optional.of(this.offer));
        DetailsView result = this.offerService.getDetails("some id");

        assertEquals("TEST", result.getSellerUsername());
        assertEquals("ITEM", result.getItemName());
        assertEquals(format, result.getAddedOn());
        assertEquals(BigDecimal.TEN, result.getPrice());
    }

    @Test
    public void testGetMyOffers() {
        when(this.repository.getAllBySeller_UsernameOrderByAddedOnDesc(anyString())).thenReturn(List.of(this.offer));
        when(this.photoService.getPhotoForThumbnail(any())).thenReturn("LOCATION");

        List<OfferView> result = this.offerService.getMyOffers("SOMETHING");

        assertEquals("TEST", result.get(0).getSellerUsername());
        assertEquals("ITEM", result.get(0).getItemName());
        assertEquals("LOCATION", result.get(0).getImageLocation());
    }

    @Test
    public void testGetByIdWithWrongId() {
        when(this.repository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.offerService.getById("TEST"));
    }

    @Test
    public void testGetByIdWith() {
        when(this.repository.findById(anyString())).thenReturn(Optional.of(this.offer));
        Offer curr = this.offerService.getById("TEST");

        assertEquals("TEST", curr.getSeller().getUsername());
        assertEquals("ITEM", curr.getItem().getName());
    }

    @Test
    public void testRemoveOfferWithInvalidId() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.offerService.removeOffer("TEST", "TEST"));
    }

    @Test
    public void testRemoveOfferWithInvalidUsername() {
        when(repository.findById(anyString())).thenReturn(Optional.of(this.offer));
        assertThrows(IllegalArgumentException.class, () -> this.offerService.removeOffer("NO SUCH USERNAME", "TEST"));
    }

    @Test
    public void testRemoveOffer() {
        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        this.offer.getItem().setForSale(false);
        when(repository.findById(anyString())).thenReturn(Optional.of(this.offer));

        this.offerService.removeOffer("TEST", "TEST");
        verify(repository).delete(captor.capture());

        assertEquals("TEST", captor.getValue().getSeller().getUsername());
        assertEquals("ITEM", captor.getValue().getItem().getName());
        assertFalse(captor.getValue().getItem().isForSale());
    }

    @Test
    public void testBuyItemWithInvalidId() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> this.offerService.buyItem("TEST", "TEST"));
    }

    @Test
    public void testBuyItemWithInvalidUsername() {
        when(repository.findById(anyString())).thenReturn(Optional.of(this.offer));
        assertThrows(IllegalArgumentException.class, () -> this.offerService.buyItem("TEST", "TEST"));
    }

    @Test
    public void testBuyItem() {
        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        when(repository.findById(anyString())).thenReturn(Optional.of(this.offer));
        when(itemService.buyItem(any(), anyString())).thenReturn(new ItemServiceModel("ITEM", "PERSON", false));

        ItemServiceModel out = this.offerService.buyItem("PERSON", "TEST");
        verify(repository).delete(captor.capture());

        assertEquals("TEST", captor.getValue().getSeller().getUsername());
        assertEquals("ITEM", captor.getValue().getItem().getName());

        assertEquals("PERSON", out.getOwnerUsername());
        assertEquals("ITEM", out.getName());
        assertFalse(out.isForSale());
    }
}