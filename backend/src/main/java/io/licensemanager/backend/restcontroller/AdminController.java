package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.entity.Role;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.model.request.CreateRoleRequest;
import io.licensemanager.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<?> activateUserByAdmin(@RequestParam(name = "user_id") final Long userId) {
        Optional<User> user = adminService.activateUserByAdmin(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s activated successfully", user.get().getUsername()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to activate user with id %d - user doesn't exist or is already activated", userId
                        )
                ));
    }

    @PostMapping(path = "/enable_user")
    public ResponseEntity<?> enableUserAccount(@RequestParam(name = "user_id") final Long userId) {
        Optional<User> user = adminService.enableUserAccount(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s enabled successfully", user.get().getUsername()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to enable user with id %d - user doesn't exist or is already enabled", userId
                        )
                ));
    }

    @PostMapping(path = "/disable_user")
    public ResponseEntity<?> disableUserAccount(@RequestParam(name = "user_id") final Long userId) {
        Optional<User> user = adminService.disableUserAccount(userId);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Collections.singletonMap("message", String.format(
                            "User %s disabled successfully", user.get().getUsername()
                            )
                    ));
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to disable user with id %d - user doesn't exist or is already disabled", userId
                        )
                ));
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
}
