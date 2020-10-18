package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.model.request.UserRegistrationRequest;
import io.licensemanager.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Collections;

@Controller
@RequestMapping("/api/auth")
public class UserRegistrationController {
    private AuthenticationManager authenticationManager;
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

    @Autowired
    public UserRegistrationController(final AuthenticationManager authenticationManager, final UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {

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

        return userService.createNewUser(registrationRequest) ?
                ResponseEntity.ok(Collections.singletonMap("message", String.format(
                        "User with username %s created successfully", registrationRequest.getUsername()))) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", String.format("Failed to register user with username %s",
                                registrationRequest.getUsername())));
    }
}
