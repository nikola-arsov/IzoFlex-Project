package bg.softuni;


import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.User;
import bg.softuni.model.enumeration.Category;
import bg.softuni.repository.CommentRepository;
import bg.softuni.repository.ItemRepository;
import bg.softuni.repository.OfferRepository;
import bg.softuni.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc()
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentRepository commentRepository;


    @BeforeEach
    public void init() {
        User owner = this.userRepository.findByUsername("admin");
        Item item = new Item();
        item.setOwner(owner);
        item.setName("ITEM");
        item.setDescription("DESCRIPTION");
        item.setCategory(Category.АЕРОПЛАСТ);
        item.setPhotos(Set.of(new Photo("LOCATION", item)));
        Item temp = this.itemRepository.saveAndFlush(item);
        this.offerRepository.saveAndFlush(new Offer(temp, owner, BigDecimal.TEN));
    }

    @AfterEach
    public void destroy() {
        this.commentRepository.deleteAll();
        this.offerRepository.deleteAll();
        this.itemRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testEditRolesPageWithAdmin() throws Exception {
        this.mockMvc.perform(get("/users/roles/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-role"));
    }

    @Test
    @WithMockUser(username = "user")
    public void testEditRolesPageWithUser() throws Exception {
        this.mockMvc.perform(get("/users/roles/edit"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetOffers() throws Exception {
        this.mockMvc.perform(get("/offers"))
                .andExpect(status().isOk())
                .andExpect(view().name("offers"))
                .andExpect(model().attributeExists("offers"))
                .andExpect(model().attributeExists("max_index"))
                .andExpect(model().attributeExists("current"))
                .andExpect(model().attribute("current", 0))
                .andExpect(model().attribute("max_index", 0));
    }

    @Test
    public void testGetOffersWithTooLargePage() throws Exception {
        this.mockMvc.perform(get("/offers").queryParam("page", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("offers"))
                .andExpect(model().attributeExists("offers"))
                .andExpect(model().attributeExists("max_index"))
                .andExpect(model().attributeExists("current"))
                .andExpect(model().attribute("current", 0))
                .andExpect(model().attribute("max_index", 0));
    }

    @Test
    public void testGetOffersWithNegativePage() throws Exception {
        this.mockMvc.perform(get("/offers").queryParam("page", "-55"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/error"));
    }

    @Test
    public void testGetOffersWithAeroplastCategory() throws Exception {
        this.mockMvc.perform(get("/offers/categories/{cat}", "аеропласт"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].itemName", is("ITEM")))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    public void testGetOffersWithAeroplastCategoryWithLargePage() throws Exception {
        this.mockMvc.perform(get("/offers/categories/{cat}", "аеропласт").param("page", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].itemName", is("ITEM")))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    public void testGetOffersWithAeroplastCategoryWithNegativePage() throws Exception {
        this.mockMvc.perform(get("/offers/categories/{cat}", "аеропласт").param("page", "-50"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/error"))
                .andExpect(flash().attribute("flag", "Page index must not be less than zero!"));
    }

    @Test
    public void testGetOffersWithAwardsCategoryWithWrongCat() throws Exception {
        this.mockMvc.perform(get("/offers/categories/{cat}", "test"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/error"))
                .andExpect(flash().attribute("flag", "test не е валидна!"));
    }

    @Test
    public void testGetCommentsForOffer() throws Exception {
        String id = this.offerRepository.findAll().get(0).getId();
        this.mockMvc.perform(get("/comments/offers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("comments"))
                .andExpect(model().attributeExists("offer"))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    public void testGetCommentsForOfferWithWrongId() throws Exception {
        this.mockMvc.perform(get("/comments/offers/{id}", "1234"))
                .andExpect(view().name("redirect:/error"))
                .andExpect(status().isFound())
                .andExpect(flash().attributeExists("flag"))
                .andExpect(flash().attribute("flag", "Offer not found!"));
    }

    @Test
    @WithUserDetails(value = "admin")
    public void testPostComments() throws Exception {
        String id = this.offerRepository.findAll().get(0).getId();

        this.mockMvc.perform(post("/comments/offers/{id}/add", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", "testtext"))
                .andExpect(view().name("redirect:/comments/offers/" + id))
                .andExpect(status().is3xxRedirection());

        assertEquals(1, this.commentRepository.count());
    }

    @Test
    @WithUserDetails(value = "admin")
    public void testPostCommentsWithInvalidInput() throws Exception {
        String id = this.offerRepository.findAll().get(0).getId();

        this.mockMvc.perform(post("/comments/offers/{id}/add", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/comments/offers/" + id))
                .andExpect(flash().attributeExists("formModel"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.formModel"));

        assertEquals(0, this.commentRepository.count());
    }

    @Test
    public void testPostCommentsWithNoUser() throws Exception {
        String id = this.offerRepository.findAll().get(0).getId();

        this.mockMvc.perform(post("/comments/offers/{id}/add", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("content", "testtext"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/users/login"));
    }
}