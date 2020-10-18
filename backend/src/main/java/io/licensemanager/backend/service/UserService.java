package io.licensemanager.backend.service;

import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.request.UserRegistrationRequest;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public boolean isUsernameAndEmailAvailable(final String username, final String email) {
        return !userRepository.existsByUsername(username) && !userRepository.existsByEmail(email);
    }

    public boolean createNewUser(final UserRegistrationRequest registrationRequest) {
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setCreationDate(LocalDateTime.now());
        user.setEmailConfirmed(false);
        user.setIsAccountActivated(false);
        user.setIsAccountActivatedByAdmin(false);
        user.setIsActive(true);

        User createdUser = userRepository.save(user);

        return createdUser.getUsername().equals(registrationRequest.getUsername());
    }
}
