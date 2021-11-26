package bg.softuni.config;


import bg.softuni.util.core.MultipartFileHandler;
import bg.softuni.util.core.ValidatorUtil;
import bg.softuni.util.core.impl.MultipartFileHandlerImpl;
import bg.softuni.util.core.impl.ValidatorUtilImpl;
import bg.softuni.util.interceptor.AdminStatisticInterceptor;
import bg.softuni.util.interceptor.OfferViewCounter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfiguration {
    @Bean
    public ValidatorUtil validatorUtil() {
        return new ValidatorUtilImpl();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AdminStatisticInterceptor adminStatisticInterceptor() {
        return new AdminStatisticInterceptor();
    }
    @Bean
    public OfferViewCounter offerViewCounter() {
        return new OfferViewCounter();
    }

    @Bean
    public MultipartFileHandler fileHandler() {
        return new MultipartFileHandlerImpl();
    }
}
