package bg.softuni.util.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

public class AdminStatisticInterceptor implements HandlerInterceptor {
    private final AtomicLong SELL;
    private final AtomicLong BUY;

    public AdminStatisticInterceptor() {
        this.SELL = new AtomicLong(0L);
        this.BUY = new AtomicLong(0L);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        String redirectUrl = (modelAndView == null) ? "" : modelAndView.getViewName();

        if ("redirect:/users/collection".equals(redirectUrl)) {
            if ("http://localhost:8080/offers/add".equals(request.getRequestURL().toString())) {
                this.SELL.incrementAndGet();
            } else {
                this.BUY.incrementAndGet();
            }
        }
    }

    public long getBuyRequestCount() {
        return this.BUY.get();
    }

    public long getSellRequestCount() {
        return this.SELL.get();
    }
}