package io.licensemanager.backend.event.publisher;

import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.event.LicenseGenerationEvent;
import io.licensemanager.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseGenerationEventPublisher {
    private final static Logger logger = LoggerFactory.getLogger(LicenseGenerationEventPublisher.class);

    private final AdminService adminService;
    private final ApplicationEventPublisher applicationEventPublisher;


    public void publishEvent(final User creator, final License createdLicense) {
        logger.debug("License generation event is being published");
        applicationEventPublisher.publishEvent(new LicenseGenerationEvent(this, creator,
                createdLicense, adminService.getNotificationsAllEmailsList()));
    }
}
