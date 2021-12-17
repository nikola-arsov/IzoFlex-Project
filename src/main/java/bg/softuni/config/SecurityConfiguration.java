package bg.softuni.config;

import bg.softuni.util.handler.LoginFailureHandler;
import bg.softuni.service.interf.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final LoginFailureHandler handler;

    @Autowired
    public SecurityConfiguration(UserService userService, PasswordEncoder encoder, LoginFailureHandler handler) {
        this.userService = userService;
        this.encoder = encoder;
        this.handler = handler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.
             userDetailsService(this.userService)
             .passwordEncoder(this.encoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
             .authorizeRequests().
                antMatchers("/offers","/offers/details/**","/offers/categories/**","/uploads/**").permitAll().
                antMatchers("/","/users/login","/users/register","/comments/offers/**").permitAll().
                antMatchers("/users/roles/edit").hasRole("ADMIN").
                antMatchers("/notifications/**").hasRole("USER").
                antMatchers("/offers/**").hasRole("USER").
                antMatchers("/users/**").hasRole("USER").
                antMatchers("/items/**").hasRole("USER").
        and()
             .formLogin().
                loginPage("/users/login").
                failureHandler(this.handler).
        and()
             .logout().
                logoutUrl("/users/logout").
                logoutSuccessUrl("/users/login?logout").
                invalidateHttpSession(true).
                deleteCookies("JSESSIONID");
    }
}