package io.licensemanager.backend.configuration.authentication.service;

import io.licensemanager.backend.configuration.authentication.AuthenticationDetails;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationDetailsService.class);

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(
                        "User %s has not been found", username))
                );

        return AuthenticationDetails.build(user);
    }
}
