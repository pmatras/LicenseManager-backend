package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.authentication.service.AuthorizationTokenService;
import io.licensemanager.backend.entity.Token;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.response.TokenVerificationResponse;
import io.licensemanager.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizationTokenController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationTokenService.class);

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String TOKEN_TYPE = "Bearer";

    private final UserService userService;
    private final AuthorizationTokenService tokenService;

    @GetMapping(path = "/token")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        Optional<String> authToken = tokenService.parseTokenFromRequest(request, AUTHORIZATION_HEADER, TOKEN_TYPE);
        if (authToken.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message",
                            "Request does not contain required header"
                            )
                    );
        }

        Optional<Token> token = tokenService.findTokenByValue(authToken.get());
        if (token.isPresent()) {
            Token userToken = token.get();
            if (tokenService.isTokenValid(userToken)) {
                User user = userToken.getUser();
                return ResponseEntity.ok(new TokenVerificationResponse(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoles()
                                .stream().map(role -> role.getName())
                                .collect(Collectors.toSet()),
                        user.getRoles()
                                .stream().flatMap(role -> role.getPermissions().stream())
                                .collect(Collectors.toSet())

                ));
            }
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", "Token is invalid"));

    }
}
