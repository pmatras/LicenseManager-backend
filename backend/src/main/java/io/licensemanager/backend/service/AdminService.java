package io.licensemanager.backend.service;

import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.RoleRepository;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Optional<User> activateUserByAdmin(final Long userId) {
        logger.info("Activating user account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && !user.get().getIsAccountActivatedByAdmin()) {
            User userToActivate = user.get();
            userToActivate.setIsAccountActivatedByAdmin(true);
            return Optional.of(userRepository.save(userToActivate));
        }
        logger.warn("Failed to activate user by admin - user doesn't exist or is already activated");

        return Optional.empty();
    }

    public Optional<User> enableUserAccount(final Long userId) {
        logger.info("Enabling user account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && !user.get().getIsActive()) {
            User userToEnable = user.get();
            userToEnable.setIsActive(true);

            return Optional.of(userRepository.save(userToEnable));
        }
        logger.warn("Failed to enable user by admin - user doesn't exist or is already enabled");

        return Optional.empty();
    }

    public Optional<User> disableUserAccount(final Long userId) {
        logger.info("Disabling user account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && user.get().getIsActive()) {
            User userToDisable = user.get();
            userToDisable.setIsActive(false);

            return Optional.of(userRepository.save(userToDisable));
        }
        logger.warn("Failed to disable user by admin - user doesn't exist or is already disabled");

        return Optional.empty();
    }

    public Optional<Role> createRoleIfNotExists(final String name, final Set<String> permissions) {
        Optional<Role> role = roleRepository.findByName(name);
        if (!role.isPresent()) {
            logger.debug("Creating new role {}", name);
            Role roleToCreate = new Role();
            roleToCreate.setName(name);
            roleToCreate.setPermissions(permissions);

            return Optional.of(roleRepository.save(roleToCreate));
        }

        return Optional.empty();
    }

    public boolean assignRolesToUser(final Long userId, final Set<String> rolesToAssign) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Set<Role> roles = roleRepository.findAllByNameIn(rolesToAssign);
            if (!roles.isEmpty()) {
                User userToAssign = user.get();
                userToAssign.setRoles(roles);
                logger.debug("Assigning roles {} to user {}", rolesToAssign.toString(), userToAssign.getUsername());

                return userRepository.save(userToAssign).getId() == userId;
            }
        }

        return false;
    }

    public List<User> getListOfUsers() {
        return userRepository.findAll();
    }

    public List<User> getListOfPendingUsers() {
        return userRepository.findAllByIsAccountActivatedByAdminFalse();
    }
}
