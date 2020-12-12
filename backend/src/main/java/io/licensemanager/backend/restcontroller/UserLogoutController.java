package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.authentication.service.AuthorizationTokenService;
import io.licensemanager.backend.configuration.setup.Operation;
import io.licensemanager.backend.event.publisher.SystemOperationEventPublisher;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Controller
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserLogoutController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationTokenService.class);

    private final String CLEAR_SITE_DATA_HEADER = "Clear-Site-Data";

    private final AuthorizationTokenService tokenService;
    private final SystemOperationEventPublisher systemOperationEventPublisher;

    @PostMapping(path = "/logout")
    public ResponseEntity<?> logoutUser(final Authentication authentication,
                                        HttpServletRequest request, HttpServletResponse response) {
        tokenService.purgeAuthorizationToken(request);

        String username = AuthenticationUtils.parseUsername(authentication);
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        systemOperationEventPublisher.publishEvent(username, Operation.LOGOUT, "User signed out");

        return ResponseEntity
                .ok()
                .header(CLEAR_SITE_DATA_HEADER, "*")
                .body(Collections.singletonMap("message", "User signed out"));
    }
}
