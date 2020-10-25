package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping(path = "/api/account")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountActivationController {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountService.class);

    private final UserAccountService accountService;

    @GetMapping(path = "/activate")
    public ResponseEntity<?> activateUserAccount(@RequestParam(name = "activation_token") String activationToken) {
        if (!accountService.isActivationTokenNotExpired(activationToken)) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Activation token is already expired or used"));
        }

        if (accountService.isUserAccountAlreadyActivated(activationToken)) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "User account is already activated"));
        }

        Optional<User> user = accountService.activateUserAccount(activationToken);
        return user.isPresent() ?
                ResponseEntity.ok(
                        Collections.singletonMap("message", String.format("User %s activated successfully", user.get().getUsername()))
                ) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Token or user already deleted"));
    }
}
