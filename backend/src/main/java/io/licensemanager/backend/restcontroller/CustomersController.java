package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.CustomerGroup;
import io.licensemanager.backend.model.request.CreateCustomerRequest;
import io.licensemanager.backend.model.request.CustomerGroupRequest;
import io.licensemanager.backend.model.request.EditCustomerRequest;
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

    @PutMapping(path = "/edit_customer")
    public ResponseEntity<?> editCustomer(@Valid @RequestBody final EditCustomerRequest request,
                                          final Authentication authentication) {
        if (!request.isValid()) {
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("message", "Customer id cannot be null, name or groups must be specified"));
        }

        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        Optional<Customer> editedCustomer = customersService.editCustomer(
                request.getCustomerId(),
                request.getNewName(),
                request.getGroups(),
                username,
                permissions
        );

        if (editedCustomer.isPresent()) {
            return ResponseEntity
                    .ok(Collections.singletonMap("message", String.format(
                            "Successfully edited customer with name %s", editedCustomer.get().getName()
                            ))
                    );
        }

        return ResponseEntity
                .badRequest()
                .body(Collections.singletonMap("message", String.format(
                        "Failed to edit customer with id %d", request.getCustomerId()
                        ))
                );
    }

    @DeleteMapping(path = "/delete_customer")
    public ResponseEntity<?> deleteCustomer(@RequestParam(name = "customer_id") final Long customerId,
                                            final Authentication authentication) {
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return customersService.deleteCustomer(customerId, username, permissions) ?
                ResponseEntity
                        .ok(Collections.singletonMap("message", "Customer successfully deleted")) :
                ResponseEntity
                        .badRequest()
                        .body(Collections.singletonMap("message", "Failed to delete customer"));
    }

}
