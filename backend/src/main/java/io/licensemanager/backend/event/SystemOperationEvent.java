package io.licensemanager.backend.event;

import io.licensemanager.backend.configuration.setup.Operation;
import org.springframework.context.ApplicationEvent;

public class SystemOperationEvent extends ApplicationEvent {
    private String username;
    private Operation operation;
    private String details;

    public SystemOperationEvent(final Object source, final String username,
                                final Operation operation, final String details) {
        super(source);
        this.username = username;
        this.operation = operation;
        this.details = details;
    }

    public String getUsername() {
        return username;
    }


    public Operation getOperation() {
        return operation;
    }

    public String getDetails() {
        return details;
    }
}
