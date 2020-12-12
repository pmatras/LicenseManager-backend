package io.licensemanager.backend.event.listener;

import io.licensemanager.backend.event.SystemOperationEvent;
import io.licensemanager.backend.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SystemOperationEventListener {

    private final static Logger logger = LoggerFactory.getLogger(SystemOperationEventListener.class);

    private final AuditService auditService;

    @Async
    @EventListener
    public void onApplicationEvent(final SystemOperationEvent event) {
        logger.debug("New {} operation event received", event.getOperation().name());
        auditService.createAuditEvent(event);
    }
}
