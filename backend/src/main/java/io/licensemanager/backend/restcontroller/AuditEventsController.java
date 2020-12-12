package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_ALL')")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditEventsController {

    private final static Logger logger = LoggerFactory.getLogger(AuditEventsController.class);

    private final AuditService auditService;

    @GetMapping(path = "/operations_list")
    public ResponseEntity<?> getOperationsList() {
        return ResponseEntity
                .ok(auditService.getOperationsList());
    }

    @GetMapping(path = "/logs")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity
                .ok(auditService.getAuditEventsList());
    }

}
