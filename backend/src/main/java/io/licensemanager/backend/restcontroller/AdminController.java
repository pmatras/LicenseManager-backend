package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.request.CreateRoleRequest;
import io.licensemanager.backend.model.request.UserRolesRequest;
import io.licensemanager.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    @PostMapping(path = "/activate_user")
    public ResponseEntity<?> activateUserByAdmin(@Valid @RequestBody final UserRolesRequest activationRequest) {
        if (!activationRequest.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Every field must not be null or blank"));
        }

        Optional<User> user = adminService.activateUserByAdmin(
                activationRequest.getUserId(),
                activationRequest.getRoles()
        );

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s activated successfully with roles %s",
                            user.get().getUsername(),
                            activationRequest.getRoles().toString()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to activate user with id %d - user doesn't exist or is already activated",
                        activationRequest.getUserId()
                        )
                ));
    }

    @PostMapping(path = "/unlock_user")
    public ResponseEntity<?> unlockUserAccount(@RequestParam(name = "user_id") final Long userId) {
        Optional<User> user = adminService.unlockUserAccount(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s unlocked successfully", user.get().getUsername()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to unlock user with id %d - user doesn't exist or is already unlocked", userId
                        )
                ));
    }

    @PostMapping(path = "/lock_user")
    public ResponseEntity<?> lockUserAccount(@RequestParam(name = "user_id") final Long userId) {
        Optional<User> user = adminService.lockUserAccount(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s locked successfully", user.get().getUsername()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to lock user with id %d - user doesn't exist or is already locked", userId
                        )
                ));
    }

    @GetMapping(path = "/users")
    public ResponseEntity<?> getListOfActivatedUsers() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String adminUsername = userDetails.getUsername();

        return ResponseEntity.ok(
                adminService.getListOfActivatedUsersExceptOne(adminUsername)
        );
    }

    @GetMapping(path = "/pending_users")
    public ResponseEntity<?> getListOfPendingUsers() {
        return ResponseEntity.ok(
                adminService.getListOfPendingUsers()
        );
    }

    @PostMapping(path = "/create_role")
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Every field must not be null or blank"));
        }

        Optional<Role> role = adminService.createRoleIfNotExists(request.getName(), request.getPermissions());

        if (role.isPresent()) {
            Role createRole = role.get();

            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "Created role with name %s", createRole.getName()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to create role, role with name %s already exists ", request.getName()
                        )
                ));
    }

    @PutMapping(path = "/edit_user_roles")
    public ResponseEntity<?> editUserRoles(@Valid @RequestBody UserRolesRequest request) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Every field must not be null or blank"));
        }

        if (adminService.editUserRoles(request.getUserId(), request.getRoles())) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "Successfully assigned roles %s to user with id %d",
                            request.getRoles().toString(),
                            request.getUserId()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", "Failed to assign roles to user"));
    }

    @PostMapping(path = "/delete_pending_user")
    public ResponseEntity<?> deletePendingUser(@RequestParam(name = "user_id") final Long userId) {
        if (adminService.deletePendingUserAccount(userId)) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "Successfully deleted pending user's account with id %d", userId
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", "User doesn't exist or isn't pending"));

    }

    @GetMapping(path = "/roles_list")
    public ResponseEntity<?> getListOfRoles() {
        return ResponseEntity.ok(
                adminService.getRolesList()
        );
    }
}
