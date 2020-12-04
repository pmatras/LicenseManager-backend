package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.model.request.LicenseRequest;
import io.licensemanager.backend.service.LicenseService;
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
import java.util.Set;

@Controller()
@RequestMapping("/api/licenses")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);

    private final LicenseService licenseService;

    @GetMapping(path = "/list")
    public ResponseEntity<?> getLicensesList(final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return ResponseEntity
                .ok(licenseService.getLicensesList(username, permissions));
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> generateNewLicense(@Valid @RequestBody final LicenseRequest request,
                                                final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "All fields must be specified"));
        }

        String username = AuthenticationUtils.parseUsername(authentication);

        Optional<License> createdLicense = licenseService.generateLicense(
                request.getName(),
                request.getExpirationDate(),
                request.getValues(),
                request.getTemplateId(),
                request.getCustomerId(),
                username
        );

        if (createdLicense.isPresent()) {
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format(
                            "Successfully created license %s for %s",
                            createdLicense.get().getName(),
                            createdLicense.get().getCustomer().getName()
                            )
                    ));
        }


        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", "Failed to create license"));
    }

    @PostMapping(path = "/disable")
    public ResponseEntity<?> disableLicense(@RequestParam(name = "license_id") final Long licenseId,
                                            final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return licenseService.disableLicense(licenseId, username, permissions) ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", "License successfully disabled")) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Failed to disable license"));
    }

    @PostMapping(path = "/reactivate")
    public ResponseEntity<?> activateLicense(@RequestParam(name = "license_id") final Long licenseId,
                                             final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return licenseService.reactivateLicense(licenseId, username, permissions) ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", "License successfully reactivated")) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Failed to reactivate license"));

    }

}
