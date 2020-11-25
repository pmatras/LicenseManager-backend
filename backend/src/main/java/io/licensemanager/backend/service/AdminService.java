package io.licensemanager.backend.service;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.ActivationTokenRepository;
import io.licensemanager.backend.repository.RoleRepository;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivationTokenRepository activationTokenRepository;

    @Transactional
    public Optional<User> activateUserByAdmin(final Long userId, final Set<String> userRoles) {
        logger.info("Activating user account by admin and assigning roles {}", userRoles);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && !user.get().getIsAccountActivatedByAdmin()) {
            User userToActivate = user.get();
            userToActivate.setIsAccountActivatedByAdmin(true);
            Set<Role> roles = roleRepository.findAllByNameIn(userRoles);
            if (!roles.isEmpty()) {
                userToActivate.setRoles(roles);
            } else {
                logger.error("Selected roles {} cannot be assigned to user - none exist", userRoles);
            }

            return Optional.of(userRepository.save(userToActivate));
        }
        logger.warn("Failed to activate user by admin - user doesn't exist or is already activated");

        return Optional.empty();
    }

    public Optional<User> unlockUserAccount(final Long userId) {
        logger.info("Unlocking user account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && !user.get().getIsActive()) {
            User userToUnlock = user.get();
            userToUnlock.setIsActive(true);

            return Optional.of(userRepository.save(userToUnlock));
        }
        logger.warn("Failed to unlock user by admin - user doesn't exist or is already unlocked");

        return Optional.empty();
    }

    public Optional<User> lockUserAccount(final Long userId) {
        logger.info("Locking user account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && user.get().getIsActive()) {
            User userToLock = user.get();
            userToLock.setIsActive(false);

            return Optional.of(userRepository.save(userToLock));
        }
        logger.warn("Failed to lock user by admin - user doesn't exist or is already locked");

        return Optional.empty();
    }

    @Transactional
    public Optional<Role> createRoleIfNotExists(final String name, final Set<String> permissions, final Set<Long> usersIds) {
        Optional<Role> role = roleRepository.findByName(name);
        if (!role.isPresent()) {
            logger.debug("Creating new role {}", name);
            Role roleToCreate = new Role();
            roleToCreate.setName(name);
            roleToCreate.setPermissions(permissions);

            Role createdRole = roleRepository.save(roleToCreate);
            logger.debug("Role created");
            if (usersIds != null && !usersIds.isEmpty()) {
                logger.debug("Assigning selected users to new role");
                Set<User> users = userRepository.findAllByIdIn(usersIds);
                if (!users.isEmpty()) {
                    users.forEach(user -> {
                        Set<Role> usersRoles = user.getRoles();
                        usersRoles.add(createdRole);
                        user.setRoles(usersRoles);
                    });

                    userRepository.saveAll(users);
                    logger.debug("Users assigned");
                }
            }

            return Optional.of(createdRole);
        }

        return Optional.empty();
    }

    public boolean editRolesPermissions(final String roleName, final Set<String> permissions) {
        logger.debug("Editing role {}", roleName);
        Optional<Role> roleToEdit = roleRepository.findByName(roleName);
        if (roleToEdit.isPresent()) {
            Role role = roleToEdit.get();
            role.setPermissions(permissions);

            return roleRepository.save(role).getName().equals(roleName);
        }
        logger.error("Requested role doesn't exist, edition aborted");

        return false;
    }

    public boolean deleteRoleIfExists(final Long roleId) {
        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) {
            if (role.get().getName().equals("ADMIN")) {
                logger.error("ADMIN role cannot be deleted!");
                return false;
            }
            Set<User> users = userRepository.findAllByRolesContains(role.get());
            if (!users.isEmpty()) {
                users.forEach(user -> {
                    Set<Role> usersRoles = user.getRoles();
                    usersRoles.remove(role.get());
                });

                userRepository.saveAll(users);
            }
            roleRepository.delete(role.get());

            return true;
        }

        return false;
    }

    public boolean editUserRoles(final Long userId, final Set<String> rolesToAssign) {
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

    public List<User> getListOfActivatedUsersExceptOne(final String username) {
        return userRepository
                .findAllByIsAccountActivatedByAdminTrueAndUsernameIsNot(username);
    }

    public List<User> getListOfPendingUsers() {
        return userRepository.findAllByIsAccountActivatedByAdminFalse();
    }

    @Transactional
    public boolean deletePendingUserAccount(final Long userId) {
        logger.debug("Deleting pending user's account by admin");
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && !user.get().getIsAccountActivatedByAdmin()) {
            activationTokenRepository.deleteAllByUserId(userId);
            userRepository.deleteById(userId);

            return true;
        }
        logger.error("Failed to delete user account with id {} - user doesn't exist or account isn't pending",
                userId);

        return false;
    }

    public List<Role> getRolesList() {
        return roleRepository.findAll();
    }

    public List<String> getAvailablePermissionsList() {
        return Stream.of(ROLES_PERMISSIONS.values())
                .map(ROLES_PERMISSIONS::name)
                .collect(Collectors.toList());
    }
}
