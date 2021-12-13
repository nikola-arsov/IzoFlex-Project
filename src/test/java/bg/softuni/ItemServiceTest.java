package bg.softuni;

import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.User;
import bg.softuni.model.enumeration.Category;
import bg.softuni.model.view.InfoItem;
import bg.softuni.repository.ItemRepository;
import bg.softuni.service.impl.ItemServiceImpl;
import bg.softuni.service.interf.ItemService;
import bg.softuni.service.interf.PhotoService;
import bg.softuni.service.interf.UserService;
import bg.softuni.util.core.MultipartFileHandler;
import bg.softuni.util.core.impl.ValidatorUtilImpl;
import bg.softuni.model.binding.ItemFormModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    private ItemService itemService;
    @Mock
    private PhotoService photoService;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository repository;
    @Mock
    private MultipartFileHandler fileHandler;
    private User admin;
    private User user;
    private Item item;

    @BeforeEach
    public void init() {
        this.itemService = new ItemServiceImpl(fileHandler, photoService, userService, repository, new ModelMapper(), new ValidatorUtilImpl());
        this.admin = new User();
        this.admin.setUsername("ADMIN");
        this.item = new Item();
        this.user = new User();
        this.user.setUsername("USER");
        this.item.setPhotos(Set.of(new Photo("unchanged", this.item)));
        this.item.setOwner(this.admin);

        File dummy = new File("uploads/dummy.jpg");
        if (!dummy.exists()) {
            if (dummy.mkdirs()) {
                new File("uploads/dummy.jpg");
            }
        }
    }

    @Test
    public void testSaveItemWithInvalidBinding() {
        ItemFormModel item = new ItemFormModel("testId", "t", "", "desc", new ArrayList<>());
        assertThrows(IllegalStateException.class, () -> this.itemService.saveItem("test", item));
    }

    @Test
    public void testSaveItemWithInvalidCategory() {
        ItemFormModel item = new ItemFormModel(null, "test", "demo", "testtesttesttesttest", List.of(new MockMultipartFile("test", new byte[50])));
        assertThrows(IllegalStateException.class, () -> this.itemService.saveItem("test", item));
    }

    @Test
    public void testSaveItemWithInvalidEmptyMultipart() {
        ItemFormModel item = new ItemFormModel(null, "test", "awards", "testtesttesttesttest", List.of(new MockMultipartFile("da", new byte[50])));
        assertThrows(IllegalStateException.class, () -> this.itemService.saveItem("test", item));
    }

    @Test
    public void testSaveItem() throws IOException {
        ItemFormModel item = new ItemFormModel(null, "test", "ЛИСТОВА_ПОДЛОЖКА", "testtesttesttesttest", List.of(new MockMultipartFile("testFile.jpg", "testFile.jpg", "image/jpg", Files.readAllBytes(Path.of("src/test/resources/testFile.jpg")))));
        when(userService.getUserByUsername(anyString())).thenReturn(this.admin);
        when(fileHandler.saveFile(anyString(), any())).thenReturn("saved");
        when(repository.saveAndFlush(any())).thenReturn(new Item());
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);

        itemService.saveItem("admin", item);
        verify(repository).saveAndFlush(captor.capture());

        assertEquals("ADMIN", captor.getValue().getOwner().getUsername());
        assertEquals("test", captor.getValue().getName());
        Assertions.assertEquals(Category.ЛИСТОВА_ПОДЛОЖКА, captor.getValue().getCategory());
        assertEquals("testtesttesttesttest", captor.getValue().getDescription());
        assertFalse(captor.getValue().isForSale());
        assertEquals(1, captor.getValue().getPhotos().size());
    }

    @Test
    public void testEditItemWithWrongCategory() {
        when(repository.findById(any())).thenReturn(Optional.of(new Item()));
        ItemFormModel item = new ItemFormModel(null, "test", "demo", "testtesttesttesttest", List.of(new MockMultipartFile("test", new byte[50])));
        assertThrows(IllegalStateException.class, () -> this.itemService.editItem("test", item));
    }

    @Test
    public void testEditItemWithWrongItemId() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        ItemFormModel item = new ItemFormModel(null, "test", "AWARDS", "testtesttesttesttest", List.of(new MockMultipartFile("test", new byte[50])));
        assertThrows(IllegalArgumentException.class, () -> this.itemService.editItem("test", item));
    }

    @Test
    public void testEditItemWithWrongUser() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        ItemFormModel item = new ItemFormModel(null, "test", "AWARDS", "testtesttesttesttest", List.of(new MockMultipartFile("name", "", "image/jpg", new byte[1])));
        assertThrows(IllegalStateException.class, () -> this.itemService.editItem("test", item));
    }

    @Test
    public void testEditItemWithUnchangedPhotos() {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        when(repository.saveAndFlush(any())).thenReturn(new Item());
        ItemFormModel item = new ItemFormModel(null, "edited", "ИЗОЛАЦИИОННА_ПОДЛОЖКА", "editededited", List.of(new MockMultipartFile("name", "", "image/jpg", new byte[1])));

        this.itemService.editItem("ADMIN", item);
        verify(repository).saveAndFlush(captor.capture());

        assertEquals("edited", captor.getValue().getName());
        Assertions.assertEquals(Category.ИЗОЛАЦИИОННА_ПОДЛОЖКА, captor.getValue().getCategory());
        assertEquals("editededited", captor.getValue().getDescription());
        assertEquals("unchanged", ((Photo) captor.getValue().getPhotos().toArray()[0]).getLocation());
    }

    @Test
    public void testEditItemWithChangedPhotos() throws IOException {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        when(repository.saveAndFlush(any())).thenReturn(new Item());
        when(fileHandler.saveFile(anyString(), any())).thenReturn("new");
        ItemFormModel item = new ItemFormModel(null, "edited", "ИЗОЛАЦИИОННА_ПОДЛОЖКА", "editededited", List.of(new MockMultipartFile("testFile.jpg", "testFile.jpg", "image/jpg", Files.readAllBytes(Path.of("src/test/resources/testFile.jpg")))));

        this.itemService.editItem("ADMIN", item);
        verify(repository).saveAndFlush(captor.capture());

        assertEquals("edited", captor.getValue().getName());
        Assertions.assertEquals(Category.ИЗОЛАЦИИОННА_ПОДЛОЖКА, captor.getValue().getCategory());
        assertEquals("editededited", captor.getValue().getDescription());
        assertEquals("new", ((Photo) captor.getValue().getPhotos().toArray()[0]).getLocation());
    }

    @Test
    public void testGetItemForInfoPageWithInvalidItemId() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemForInfoPage("SOME", "SOME"));
    }

    @Test
    public void testGetItemForInfoPageWithInvalidPrincipal() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemForInfoPage("SOME", "SOME"));
    }

    @Test
    public void testGetItemForInfoPage() {
        this.item.setName("INFO");
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        when(photoService.getPhotoForThumbnail(any())).thenReturn("thumbnail");

        InfoItem info = this.itemService.getItemForInfoPage("SOME", "ADMIN");
        assertEquals("thumbnail", info.getPhoto());
        assertEquals("ADMIN", info.getOwnerUsername());
        assertEquals("INFO", info.getName());
    }

    @Test
    public void testToggleForSaleExpectTrue() {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        when(repository.saveAndFlush(any())).thenReturn(this.item);

        this.itemService.toggleForSale(this.item);
        verify(repository).saveAndFlush(captor.capture());

        assertTrue(captor.getValue().isForSale());
    }

    @Test
    public void testToggleForSaleExpectFalse() {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        when(repository.saveAndFlush(any())).thenReturn(this.item);
        this.item.setForSale(true);

        this.itemService.toggleForSale(this.item);
        verify(repository).saveAndFlush(captor.capture());

        assertFalse(captor.getValue().isForSale());
    }

    @Test
    public void testGetItemViewByIdWithWrongId() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemViewById("SOME", "SOME"));
    }

    @Test
    public void testGetItemViewByIdWithWrongPrincipal() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemViewById("SOME", "SOME"));
    }

    @Test
    public void testGetItemViewByIdWithItemForSale() {
        this.item.setForSale(true);
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.getItemViewById("ADMIN", "SOME"));
    }

    @Test
    public void testGetItemView() {
        this.item.setName("VIEW");
        this.item.setDescription("DESCRIPTION");
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        ItemFormModel result = itemService.getItemViewById("ADMIN", "SOME");

        assertEquals("VIEW", result.getName());
        assertEquals("DESCRIPTION", result.getDescription());
    }

    @Test
    public void testValidateOwnerAndItemIdWithWrongItemId() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> itemService.validateOwnerAndItemId("SOME", "ADMIN", "MESSAGE"));
    }

    @Test
    public void testValidateOwnerAndItemIdWithWrongUser() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.validateOwnerAndItemId("SOME", "ADSADDA", "MESSAGE"));
    }

    @Test
    public void testValidateOwnerAndItemId() {
        this.item.setName("VALIDATE");
        this.item.setDescription("VALIDATE");
        this.item.setCategory(Category.ИЗОЛАЦИИОННА_ПОДЛОЖКА);
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        Item out = itemService.validateOwnerAndItemId("SOME", "ADMIN", "MESSAGE");

        assertEquals("VALIDATE", out.getName());
        assertEquals("VALIDATE", out.getDescription());
        Assertions.assertEquals(Category.ИЗОЛАЦИИОННА_ПОДЛОЖКА, out.getCategory());
    }

    @Test
    public void testRemoveItemWithWrongItemId() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> itemService.removeItem("SOME", "ADMIN"));
    }

    @Test
    public void testRemoveItemWithWrongPrincipal() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.removeItem("SOME", "SEXY"));
    }

    @Test
    public void testRemoveItemWithItemForSale() {
        this.item.setForSale(true);
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.removeItem("SOME", "ADMIN"));
    }

    @Test
    public void testRemoveItemWithInvalidPhotos() {
        when(repository.findById(any())).thenReturn(Optional.of(this.item));
        assertThrows(IllegalArgumentException.class, () -> itemService.removeItem("SOME", "ADMIN"));
    }

    @Test
    public void testRemoveItem() {
        this.item.setPhotos(new HashSet<>());
        this.item.getPhotos().add(new Photo("/uploads/dummy.jpg", this.item));
        this.item.setName("DELETE");
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);

        when(repository.findById(any())).thenReturn(Optional.of(this.item));

        itemService.removeItem("SOME", "ADMIN");
        verify(repository).delete(captor.capture());

        assertEquals("DELETE", captor.getValue().getName());
        assertEquals("/uploads/dummy.jpg", ((Photo) captor.getValue().getPhotos().toArray()[0]).getLocation());
        assertFalse(Files.exists(Path.of("uploads/dummy.jpg")));
    }

    @Test
    public void buyItemWithSameSellerAsBuyer() {
        Offer offer = new Offer(this.item, this.admin, BigDecimal.TEN);
        offer.getItem().setForSale(true);
        assertThrows(IllegalArgumentException.class, () -> itemService.buyItem(offer, "ADMIN"));
    }

    @Test
    public void buyItem() {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        Offer offer = new Offer(this.item, this.admin, BigDecimal.TEN);
        offer.getItem().setForSale(true);
        offer.getItem().setName("BUY");

        when(userService.getUserByUsername(anyString())).thenReturn(this.user);
        when(this.repository.saveAndFlush(any())).thenReturn(offer.getItem());

        itemService.buyItem(offer, "USER");
        verify(repository).saveAndFlush(captor.capture());

        assertEquals("USER",captor.getValue().getOwner().getUsername());
        assertEquals("BUY",captor.getValue().getName());
        assertFalse(captor.getValue().isForSale());
    }
}