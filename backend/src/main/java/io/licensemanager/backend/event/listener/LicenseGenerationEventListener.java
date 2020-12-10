package io.licensemanager.backend.event.listener;

import io.licensemanager.backend.event.LicenseGenerationEvent;
import io.licensemanager.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseGenerationEventListener {

    private final static Logger logger = LoggerFactory.getLogger(LicenseGenerationEventListener.class);

    private final EmailService emailService;

    @Async
    @EventListener
    public void onApplicationEvent(LicenseGenerationEvent event) {
        logger.debug("Sending e-mail about license generation to administrators");
        emailService.sendEmailMessageToMultipleRecipients("Somebody has generated new license",
                event.getEmailsList(), event.getMessage());
        logger.debug("Sending e-mail about license generation to administrators finished");
    }
}
