package io.licensemanager.backend.event.publisher;

import io.licensemanager.backend.configuration.setup.Operation;
import io.licensemanager.backend.event.SystemOperationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SystemOperationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(final String username, final Operation operation,
                             final String details) {
        applicationEventPublisher.publishEvent(new SystemOperationEvent(
                this, username, operation, details)
        );
    }

}
