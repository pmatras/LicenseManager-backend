package io.licensemanager.backend.service;

import io.licensemanager.backend.entity.AuditEvent;
import io.licensemanager.backend.event.SystemOperationEvent;
import io.licensemanager.backend.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditService {

    private final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditRepository;

    @Transactional
    public void createAuditEvent(final SystemOperationEvent event) {
        logger.debug("Persisting new {} operation", event.getOperation().name());
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setTimestamp(Instant.ofEpochMilli(event.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        );
        auditEvent.setUsername(event.getUsername());
        auditEvent.setOperation(event.getOperation());
        auditEvent.setDetails(event.getDetails());

        auditRepository.save(auditEvent);
    }
    
}
