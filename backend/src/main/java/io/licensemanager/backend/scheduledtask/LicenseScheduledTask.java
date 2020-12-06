package io.licensemanager.backend.scheduledtask;


import io.licensemanager.backend.model.LicensesStatus;
import io.licensemanager.backend.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(LicenseScheduledTask.class);

    private final LicenseService licenseService;

    @Scheduled(fixedRate = 60 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void checkLicensesValidityTask() {
        logger.info("Checking licenses validity task started");
        LicensesStatus licensesStatus = licenseService.checkLicensesValidity();
        logger.info("Checking licenses validity task finished");
        logger.info("{} license(s) valid and {} license(s) expired",
                licensesStatus.getValidLicensesCount(),
                licensesStatus.getExpiredLicensesCount()
        );
    }
}
