package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.request.UserRegistrationRequest;
import io.licensemanager.backend.service.UserAccountService;
import io.licensemanager.backend.service.UserService;
import io.licensemanager.backend.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

    private final UserService userService;
    private final UserAccountService userAccountService;

    @PostMapping(path = "/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest,
                                          HttpServletRequest request) {

        if (!registrationRequest.isValidRequest()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Every field must not be null or blank"));
        }

        if (!userService.isUsernameAndEmailAvailable(registrationRequest.getUsername(), registrationRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "User with specified username or e-mail already exists"));
        }

        User createdUser = userService.createNewUser(registrationRequest);

        if (createdUser.getUsername().equals(registrationRequest.getUsername())) {
            String activationToken = userAccountService.generateActivationToken();
            userAccountService.assignActivationTokenToUser(activationToken, createdUser);

            String activationURL = String.format("%s/api/account/activate?activation_token=%s",
                    RequestUtils.getServerBaseURL(request), activationToken);

            userAccountService.sendActivationEmail(createdUser, activationURL);

            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User with username %s registered successfully, activation e-mail sent to %s",
                            createdUser.getUsername(),
                            createdUser.getEmail()
                            )
                    )
            );
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Collections.singletonMap("message", String.format(
                                "Failed to register user with username %s", registrationRequest.getUsername()
                                )
                        )
                );
    }
}
