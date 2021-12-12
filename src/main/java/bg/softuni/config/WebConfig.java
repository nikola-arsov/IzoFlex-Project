package bg.softuni.config;

import bg.softuni.util.interceptor.AdminStatisticInterceptor;
import bg.softuni.util.interceptor.OfferViewCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminStatisticInterceptor interceptor;
    private final OfferViewCounter counterInterceptor;

    @Autowired
    public WebConfig(AdminStatisticInterceptor interceptor, OfferViewCounter counterInterceptor) {
        this.interceptor = interceptor;
        this.counterInterceptor = counterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.interceptor).addPathPatterns("/offers/add","/offers/buy/**");
        registry.addInterceptor(this.counterInterceptor).addPathPatterns("/offers/details/**");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
