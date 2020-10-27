package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.authentication.service.AuthorizationTokenService;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.request.UserLoginRequest;
import io.licensemanager.backend.model.response.UserLoginResponse;
import io.licensemanager.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationTokenService tokenService;

    @PostMapping(path = "/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest loginRequest,
                                       @Nullable @RequestHeader(value = "User-Agent") String userAgent) {
        if (!loginRequest.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Every field must not be null or blank"));
        }

        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword())
            );
        } catch (Exception e) {
            logger.error("Failed to authenticate user, {}", e.getMessage().toLowerCase());

            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", String.format(
                            "Failed to sign in, %s", e.getMessage().toLowerCase())
                            )
                    );
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenService.getAuthorizationTokenForUser(userAgent);
        Optional<User> user = userService.findUserByUsername(loginRequest.getUsername());
        if (user.isPresent()) {
            User authenticatedUser = user.get();
            tokenService.assignTokenToUser(token, userAgent, authenticatedUser);

            ResponseEntity.ok(new UserLoginResponse(
                    "Authentication successful",
                    authenticatedUser.getUsername(),
                    token,
                    authenticatedUser.getRoles()
                            .stream().map(role -> role.getName())
                            .collect(Collectors.toSet()),
                    authenticatedUser.getRoles()
                            .stream().flatMap(role -> role.getPermissions().stream())
                            .collect(Collectors.toSet())
            ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", "Failed to authenticate"));
    }
}
