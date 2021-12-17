package bg.softuni;

import bg.softuni.util.interceptor.AdminStatisticInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AdminStatisticInterceptorTest {
    private AdminStatisticInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void init() {
        this.interceptor = new AdminStatisticInterceptor();
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Test
    public void testHandlerWithURLItShouldIgnore() {
        ModelAndView view = new ModelAndView();
        view.setViewName("redirect:/");
        this.request.setRequestURI(":8080/users/login");

        this.interceptor.postHandle(this.request, this.response, new Object(), view);
        assertEquals(0,this.interceptor.getBuyRequestCount());
        assertEquals(0,this.interceptor.getSellRequestCount());
    }
    @Test
    public void testHandlerWithBuyURL() {
        ModelAndView view = new ModelAndView();
        view.setViewName("redirect:/users/collection");

        this.request.setRequestURI(":8080/offers/buy/id");
        this.interceptor.postHandle(this.request, this.response, new Object(), view);

        this.request.setRequestURI(":8080/offers/add");
        this.interceptor.postHandle(this.request, this.response, new Object(), view);

        assertEquals(1,this.interceptor.getBuyRequestCount());
        assertEquals(1,this.interceptor.getSellRequestCount());
    }
}