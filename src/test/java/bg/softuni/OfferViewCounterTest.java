package bg.softuni;

import bg.softuni.util.interceptor.OfferViewCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OfferViewCounterTest {
    private final String ITEM_ONE_URL = "/offers/details/one";
    private final String ITEM_TWO_URL = "/offers/details/two";

    private OfferViewCounter interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    @Mock
    private Principal principal;

    @BeforeEach
    public void init() {
        this.interceptor = new OfferViewCounter();
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Test
    public void testHandlerWithURLItShouldIgnore() {
        ModelAndView view = new ModelAndView();
        view.setViewName("redirect:/users/login");
        this.request.setRequestURI(":8080/users/register");

        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        assertEquals("видяна 0 пъти", this.interceptor.getForUrl("/users/register"));
    }

    @Test
    public void testHandlerWithBuyURL() {
        ModelAndView view = new ModelAndView();
        view.setViewName("details");
        when(principal.getName()).thenReturn("1");

        this.request.setRequestURI(":8080" + ITEM_ONE_URL);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        when(principal.getName()).thenReturn("2");
        request.setUserPrincipal(this.principal);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);

        this.request.setRequestURI(":8080" + ITEM_TWO_URL);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        when(principal.getName()).thenReturn("3");
        request.setUserPrincipal(this.principal);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        when(principal.getName()).thenReturn("4");
        request.setUserPrincipal(this.principal);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);

        assertEquals("видяна 2 пъти", this.interceptor.getForUrl(ITEM_ONE_URL));
        assertEquals("видяна 3 пъти", this.interceptor.getForUrl(ITEM_TWO_URL));
    }

    @Test
    public void testDeleteEntryWithInvalidKey() {
        assertEquals(0, interceptor.deleteEntry("HUI"));
    }

    @Test
    public void testDeleteEntry() {
        ModelAndView view = new ModelAndView();
        view.setViewName("details");
        this.request.setRequestURI(":8080" + ITEM_ONE_URL);
        this.interceptor.postHandle(this.request, this.response, new Object(), view);

        assertEquals(1, interceptor.deleteEntry(ITEM_ONE_URL));
        assertEquals("видяна 0 пъти", this.interceptor.getForUrl(ITEM_ONE_URL));
    }
}