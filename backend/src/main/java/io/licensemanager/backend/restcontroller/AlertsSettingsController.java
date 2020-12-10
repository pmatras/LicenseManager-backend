package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.EmailAlert;
import io.licensemanager.backend.model.request.EmailAlertRequest;
import io.licensemanager.backend.service.AlertsSettingsService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/api/alerts")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertsSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(AlertsSettingsController.class);

    private final AlertsSettingsService alertsService;

    @GetMapping(path = "/email_settings")
    public ResponseEntity<?> getEmailAlertSettings(final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Optional<EmailAlert> emailAlert = alertsService.getEmailAlertSettingsForUser(username);
        return emailAlert.isPresent() ?
                ResponseEntity
                        .ok(emailAlert.get()) :
                ResponseEntity
                        .ok("{}");
    }

    @PutMapping(path = "/email_settings")
    public ResponseEntity<?> updateEmailAlertSettings(@Valid @RequestBody final EmailAlertRequest request,
                                                      final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message",
                            "Threshold value must be greater than zero, all alert's settings must be specified"));
        }

        String username = AuthenticationUtils.parseUsername(authentication);

        return alertsService.updateEmailAlertsSettings(username, request.getThreshold(),
                request.getActiveHoursFrom(), request.getActiveHoursTo()
        ) ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", "Email alert settings successfully updated")) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Failed to update email alerts settings"));
    }
}
