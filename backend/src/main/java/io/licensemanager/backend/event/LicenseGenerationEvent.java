package io.licensemanager.backend.event;

import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.User;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class LicenseGenerationEvent extends ApplicationEvent {
    private User creator;
    private License createdLicense;
    private List<String> emailsList;

    public LicenseGenerationEvent(final Object source, final User creator, final License createdLicense,
                                  final List<String> emailsList) {
        super(source);
        this.creator = creator;
        this.createdLicense = createdLicense;
        this.emailsList = emailsList;
    }

    public List<String> getEmailsList() {
        return emailsList;
    }

    public String getMessage() {
        return String.format("User %s %s with e-mail %s just generated license %s for customer %s",
                creator.getFirstName(),
                creator.getLastName(),
                creator.getEmail(),
                createdLicense.getName(),
                createdLicense.getCustomer().getName()
        );
    }
}
