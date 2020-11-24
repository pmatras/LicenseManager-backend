package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.CustomerGroup;
import io.licensemanager.backend.model.request.CreateCustomerRequest;
import io.licensemanager.backend.model.request.CustomerGroupRequest;
import io.licensemanager.backend.service.CustomersService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/api/customers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomersController {

    private static final Logger logger = LoggerFactory.getLogger(CustomersController.class);

    private final CustomersService customersService;

    @GetMapping(path = "/customers_list")
    public ResponseEntity<?> getCustomersList(final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return ResponseEntity.ok(
                customersService.getCustomersList(username, permissions)
        );
    }

    @GetMapping(path = "/groups_list")
    public ResponseEntity<?> getCustomersGroupsForUser(final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);

        return ResponseEntity.ok(
                customersService.getCustomersGroupsList(username)
        );
    }

    @PostMapping(path = "/create_customer")
    public ResponseEntity<?> createCustomerIfNotExists(@Valid @RequestBody final CreateCustomerRequest request,
                                                       final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Customer's name cannot be empty or null"));
        }

        String username = AuthenticationUtils.parseUsername(authentication);

        Optional<Customer> createdCustomer = customersService.createCustomerIfNotExists(
                request.getName(), request.getGroups(), username);

        if (createdCustomer.isPresent()) {
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format(
                            "Successfully created customer with name %s", createdCustomer.get().getName()
                            ))
                    );
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to create customer with name %s", request.getName()
                        ))
                );
    }

    @PostMapping(path = "/create_group")
    public ResponseEntity<?> createGroupIfNotExists(@Valid @RequestBody final CustomerGroupRequest request,
                                                    final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Group name cannot be empty or null"));
        }

        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        Optional<CustomerGroup> createdGroup = customersService.createGroupIfNotExists(
                request.getName(),
                request.getDisplayColor(),
                request.getCustomersIds(),
                username,
                permissions
        );

        if (createdGroup.isPresent()) {
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format(
                            "Successfully created group with name %s", createdGroup.get().getName()
                            ))
                    );
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to create group with name %s", request.getName()
                        ))
                );
    }

}
