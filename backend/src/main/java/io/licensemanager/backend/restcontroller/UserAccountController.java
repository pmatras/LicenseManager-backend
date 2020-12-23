package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.OperationStatus;
import io.licensemanager.backend.model.request.EditAccountRequest;
import io.licensemanager.backend.service.UserAccountService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping(path = "/api/account")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAccountController {

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

    @PutMapping("/edit")
    public ResponseEntity<?> editUserAccount(@Valid @RequestBody final EditAccountRequest request,
                                             final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);

        OperationStatus status = accountService.editUserAccount(username, request.getCurrentPassword(),
                request.getUsername(), request.getPassword());

        return status.getStatus() ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", status.getMessage())) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", status.getMessage()));
    }

}
