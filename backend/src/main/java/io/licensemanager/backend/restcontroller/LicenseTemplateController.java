package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.LicenseTemplate;
import io.licensemanager.backend.model.request.LicenseTemplateRequest;
import io.licensemanager.backend.service.LicenseTemplateService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/api/templates")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseTemplateController.class);

    private final LicenseTemplateService licenseTemplateService;

    @GetMapping(path = "/list")
    public ResponseEntity<?> getLicenseTemplatesList() {
        return ResponseEntity
                .ok(licenseTemplateService.getLicenseTemplatesList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/supported_types")
    public ResponseEntity<?> getSupportedFieldTypes() {
        return ResponseEntity
                .ok(licenseTemplateService.getSupportedFieldTypes());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/create")
    public ResponseEntity<?> createLicenseTemplate(@Valid @RequestBody final LicenseTemplateRequest request,
                                                   final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap(
                            "message", "Name cannot be empty or null, fields must contain at least one element"
                    ));
        }

        String username = AuthenticationUtils.parseUsername(authentication);

        Optional<LicenseTemplate> createdTemplate = licenseTemplateService.createLicenseTemplate(
                request.getName(),
                request.getFields(),
                username
        );

        if (createdTemplate.isPresent()) {
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format(
                            "Successfully created license template with name %s", createdTemplate.get().getName()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to create license template with name %s - name already exists", request.getName()
                        )
                ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteLicenseTemplate(@RequestParam(name = "template_id") final Long templateId) {
        return licenseTemplateService.deleteLicenseTemplate(templateId) ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", "License template successfully deleted")) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Failed to delete license template"));
    }
}
