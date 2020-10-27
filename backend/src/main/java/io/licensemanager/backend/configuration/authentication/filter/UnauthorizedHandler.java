package io.licensemanager.backend.configuration.authentication.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(UnauthorizedHandler.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Authentication error: {}", authException.getMessage().toLowerCase());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized request");
    }
}
