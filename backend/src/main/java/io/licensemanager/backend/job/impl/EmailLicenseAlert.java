package io.licensemanager.backend.job.impl;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.EmailAlert;
import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.job.LicenseAlertJob;
import io.licensemanager.backend.repository.EmailAlertRepository;
import io.licensemanager.backend.repository.LicenseRepository;
import io.licensemanager.backend.service.EmailService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailLicenseAlert implements LicenseAlertJob {

    private static final Logger logger = LoggerFactory.getLogger(EmailLicenseAlert.class);

    private final EmailAlertRepository alertRepository;
    private final EmailService emailService;
    private final LicenseRepository licenseRepository;

    private static String emailAlertHeader = "Some licenses are expiring soon" +
            ":<br><br>" +
            "<table border=\"1px solid\" style=\"border-collapse: collapse;\">" +
            "<tr><th>License Name</th><th>Customer</th><th>Expiration Date</th></tr>";

    public List<EmailAlert> getEmailAlertsSettings() {
        return alertRepository.findAll();
    }

    private List<License> getLicensesForUser(final User user) {
        Set<ROLES_PERMISSIONS> userPermissions = AuthenticationUtils.getUserPermissions(user);
        if (userPermissions.contains(ROLES_PERMISSIONS.ALL) ||
                userPermissions.contains(ROLES_PERMISSIONS.NOTIFICATIONS_ALL_LICENSES) ||
                userPermissions.contains(ROLES_PERMISSIONS.NOTIFICATIONS_ALL)) {
            return licenseRepository.findAll();
        }

        return licenseRepository.findAllByCreatorIs(user);
    }

    private BiPredicate<License, Integer> alertPredicate = ((license, threshold) -> {
        LocalDateTime currentDate = LocalDateTime.now();
        return
                !license.getIsExpired() &&
                        Duration.between(currentDate, license.getExpirationDate()).toDays() <= threshold;
    });

    private List<License> getNotifiedLicenses(final List<License> licenses, final Integer alertThreshold) {
        return licenses.stream()
                .filter(license -> alertPredicate.test(license, alertThreshold))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long sendAlerts() {
        AtomicLong sentEmails = new AtomicLong();
        List<EmailAlert> alertsSettings = getEmailAlertsSettings();
        alertsSettings.forEach(alertSetting -> {
            User user = alertSetting.getUser();
            Integer thresholdDays = alertSetting.getThreshold();
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isAfter(alertSetting.getActiveHoursFrom()) &&
                    currentTime.isBefore(alertSetting.getActiveHoursTo())) {
                List<License> licenses = getNotifiedLicenses(
                        getLicensesForUser(user),
                        thresholdDays
                );
                if (!licenses.isEmpty()) {
                    StringBuilder emailBody = new StringBuilder(emailAlertHeader);
                    licenses.forEach(license ->
                            emailBody.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>",
                                    license.getName(),
                                    license.getCustomer().getName(),
                                    license.getExpirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            ))
                    );
                    emailBody.append("</table>");
                    emailService.sendHtmlEmailMessage("License(s) expiration", user.getEmail(), emailBody.toString());
                    sentEmails.getAndIncrement();
                }
            }
        });

        return sentEmails.get();
    }
}
