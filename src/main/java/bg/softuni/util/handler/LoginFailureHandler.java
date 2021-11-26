package bg.softuni.util.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    public static final String ERROR = "FLASH_AUTHENTICATION_MESSAGE";
    public static final String USERNAME = "FLASH_USERNAME";

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {
        if (exception != null) {
            final FlashMap flashMap = new FlashMap();

            flashMap.put(ERROR, exception.getMessage());
            flashMap.put(USERNAME, request.getParameter("username"));

            final FlashMapManager flashMapManager = new SessionFlashMapManager();
            flashMapManager.saveOutputFlashMap(flashMap, request, response);
        }
        response.sendRedirect("/users/login");
    }
}
