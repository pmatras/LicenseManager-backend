package io.licensemanager.backend.service;

import io.licensemanager.backend.configuration.setup.Operation;
import io.licensemanager.backend.entity.ActivationToken;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.event.publisher.SystemOperationEventPublisher;
import io.licensemanager.backend.model.OperationStatus;
import io.licensemanager.backend.repository.ActivationTokenRepository;
import io.licensemanager.backend.repository.UserRepository;
import io.licensemanager.backend.util.TimeTokensParser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAccountService {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountService.class);

    private TemporalAmount ACTIVATION_TOKEN_TTL_VALUE;

    private final ActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SystemOperationEventPublisher systemOperationEventPublisher;

    @Value("${activation-token.ttl:3D}")
    public void setActivationTokenTTLValue(String tokenTTLValue) {
        this.ACTIVATION_TOKEN_TTL_VALUE = TimeTokensParser.parseTimeToken(tokenTTLValue);
    }

    public String generateActivationToken() {
        logger.debug("Generating new verification token");
        return UUID.randomUUID().toString();
    }

    public boolean assignActivationTokenToUser(String token, User user) {
        ActivationToken activationToken = new ActivationToken();
        activationToken.setValue(token);
        activationToken.setUser(user);
        LocalDateTime currentTimestamp = LocalDateTime.now();
        activationToken.setCreationDate(currentTimestamp);
        activationToken.setExpirationDate(currentTimestamp.plus(ACTIVATION_TOKEN_TTL_VALUE));

        ActivationToken assignedToken = activationTokenRepository.save(activationToken);

        return assignedToken.getUser().getId() == user.getId();
    }

    public boolean sendActivationEmail(final User user, final String activationURL) {
        logger.debug("Sending activation link to {}", user.getEmail());

        StringBuilder htmlEmailContent = new StringBuilder();
        htmlEmailContent
                .append(String.format("Hi <b>%s</b>,", user.getUsername()))
                .append("<br><br>")
                .append("Please click the following link to activate your account:")
                .append("<br><br>")
                .append(String.format("<a href=\"%s\"><b>Click to activate your account</b></a>", activationURL))
                .append("<br><br>")
                .append("<hr>")
                .append("license-manager");

        return emailService.sendHtmlEmailMessage("Account activation", user.getEmail(), htmlEmailContent.toString());
    }

    private Optional<ActivationToken> getTokenForUserIfAlreadyExists(final Long userId) {
        return activationTokenRepository.findByUserId(userId);
    }

    public boolean isActivationTokenNotExpired(final String token) {
        Optional<ActivationToken> activationToken = activationTokenRepository.findByValue(token);
        if (activationToken.isPresent()) {
            return LocalDateTime.now()
                    .isBefore(activationToken.get().getExpirationDate());
        }

        return false;
    }

    public boolean isUserAccountAlreadyActivated(final String token) {
        Optional<ActivationToken> activationToken = activationTokenRepository.findByValue(token);
        if (activationToken.isPresent()) {
            return activationToken.get()
                    .getUser().getIsAccountActivated();
        }

        return false;
    }

    public Optional<User> activateUserAccount(final String token) {
        logger.info("Activating user account");
        Optional<ActivationToken> activationToken = activationTokenRepository.findByValue(token);
        if (activationToken.isPresent()) {
            Optional<User> user = Optional.ofNullable(activationToken.get().getUser());
            if (user.isPresent()) {
                User userToActivate = user.get();
                userToActivate.setIsAccountActivated(true);
                userToActivate.setEmailConfirmed(true);
                activationTokenRepository.delete(activationToken.get());
                logger.info("User account activated, activation token deleted");
                return Optional.of(userRepository.save(userToActivate));
            }
        }

        return Optional.empty();
    }

    public OperationStatus editUserAccount(final String currentUsername, final String currentPassword,
                                           final Optional<String> username, final Optional<String> password) {
        logger.debug("Editing user account");
        Optional<User> user = userRepository.findByUsername(currentUsername);
        if (user.isEmpty()) {
            logger.error("User with passed username doesn't exist");
            return new OperationStatus(false, "Requested user doesn't exist");
        }

        if (!passwordEncoder.matches(currentPassword, user.get().getPassword())) {
            logger.error("Wrong password provided, aborting");
            return new OperationStatus(false, "Wrong password");
        }

        if (username.isEmpty() && password.isEmpty()) {
            logger.error("Neither username nor password are specified, aborting operation");
            return new OperationStatus(false, "Nothing to change");
        }

        User userToEdit = user.get();
        boolean usernameChanged = false;
        boolean passwordChanged = false;

        if (username.isPresent()) {
            if (!username.get().equals(currentUsername) && userRepository.existsByUsername(username.get())) {
                logger.error("Username is already taken");
                return new OperationStatus(false, "Username is already taken");
            }
            if (StringUtils.isBlank(username.get())) {
                logger.error("Username cannot be blank");
                return new OperationStatus(false, "Username cannot be blank");
            }
            userToEdit.setUsername(username.get());
            usernameChanged = true;
        }
        if (password.isPresent()) {
            if (StringUtils.isBlank(password.get())) {
                logger.error("Password cannot be blank");
                return new OperationStatus(false, "Password cannot be blank!");
            }
            userToEdit.setPassword(passwordEncoder.encode(password.get()));
            passwordChanged = true;
        }
        userRepository.save(userToEdit);

        StringBuilder message = new StringBuilder(usernameChanged ?
                String.format("Username changed from %s to %s", currentUsername, username.get()) :
                ""
        );
        message.append(passwordChanged ? ", password changed" : ", password left intact");
        systemOperationEventPublisher.publishEvent(currentUsername, Operation.ACCOUNT_EDITION, message.toString());

        return new OperationStatus(true, "User account successfully updated, refresh page to see effects");
    }
}
