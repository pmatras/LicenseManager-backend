package io.licensemanager.backend.scheduledtask;

import io.licensemanager.backend.job.impl.EmailLicenseAlert;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(AlertScheduledTask.class);

    private final EmailLicenseAlert emailAlert;

    @Async
    @Scheduled(cron = "0 0 10 * * MON-FRI")
    public void sendEmailAlert() {
        logger.info("Email alerts sending started");
        logger.info("{} email(s) sent", emailAlert.sendAlerts());
        logger.info("Email alerts sending finished");
    }
}
