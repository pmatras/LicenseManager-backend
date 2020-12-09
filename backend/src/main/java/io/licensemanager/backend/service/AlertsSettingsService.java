package io.licensemanager.backend.service;

import io.licensemanager.backend.entity.EmailAlert;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.UserRepository;
import io.licensemanager.backend.repository.EmailAlertRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertsSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(AlertsSettingsService.class);

    private final EmailAlertRepository emailAlertRepository;
    private final UserRepository userRepository;

    private static final LocalTime ACTIVE_TIME_FROM_DEFAULT = LocalTime.of(0, 0);
    private static final LocalTime ACTIVE_TIME_TO_DEFAULT = LocalTime.of(23, 59);

    public Optional<EmailAlert> getEmailAlertSettingsForUser(final String username) {
        logger.debug("Getting email alert settings for user");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return Optional.empty();
        }

        return emailAlertRepository.findByUserId(user.get().getId());
    }

    public boolean updateEmailAlertsSettings(final String username, final Integer thresholdDays,
                                             final String activeTimeFrom, final String activeTimeTo) {
        logger.debug("Updating e-mail alert config for user");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return false;
        }
        Optional<EmailAlert> emailAlert = emailAlertRepository.findByUserId(user.get().getId());
        EmailAlert alert = emailAlert.orElseGet(() -> new EmailAlert());

        alert.setUser(user.get());
        alert.setThreshold(thresholdDays);
        try {
            alert.setActiveHoursFrom(LocalTime.parse(activeTimeFrom));
            alert.setActiveHoursTo(LocalTime.parse(activeTimeTo));
        } catch (DateTimeParseException e) {
            logger.error("Cannot parse active time range of email alert - {}, default values will be used", e.getMessage());
            alert.setActiveHoursFrom(ACTIVE_TIME_FROM_DEFAULT);
            alert.setActiveHoursTo(ACTIVE_TIME_TO_DEFAULT);
        }
        emailAlertRepository.save(alert);

        return true;
    }

}
