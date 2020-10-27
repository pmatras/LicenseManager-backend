package io.licensemanager.backend.configuration.setup;

import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.RoleRepository;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InitialDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String ADMIN_PERMISSION_NAME = "ALL";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @PostConstruct
    public void initializeData() {
        Role adminRole = createDefaultAdminRoleIfNotExists();
        createDefaultAdminUserIfNotExists(Set.of(adminRole));
    }

    private Role createDefaultAdminRoleIfNotExists() {
        Optional<Role> adminRole = roleRepository.findByName(ADMIN_ROLE_NAME);
        if (adminRole.isEmpty()) {
            logger.info("Creating default admin role in system");
            Role role = new Role();
            role.setName(ADMIN_ROLE_NAME);
            role.setPermissions(Set.of(ADMIN_PERMISSION_NAME));

            return roleRepository.save(role);
        }
        logger.info("Default admin role already exists in system, creation skipped");

        return adminRole.get();
    }

    private void createDefaultAdminUserIfNotExists(Set<Role> adminRoles) {
        Optional<User> user = userRepository.findByUsername(ADMIN_USERNAME);
        if (user.isEmpty()) {
            logger.info("Creating default admin user in system");
            User adminUser = new User();
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            adminUser.setRoles(adminRoles);
            adminUser.setEmail("admin@license-manager.io");
            adminUser.setEmailConfirmed(true);
            adminUser.setFirstName("License-manager");
            adminUser.setLastName("Administrator");
            adminUser.setIsActive(true);
            adminUser.setIsAccountActivated(true);
            adminUser.setIsAccountActivatedByAdmin(true);
            adminUser.setCreationDate(LocalDateTime.now());

            userRepository.save(adminUser);
        }

        logger.info("Default admin user already exists in system, creation skipped");
    }
}
