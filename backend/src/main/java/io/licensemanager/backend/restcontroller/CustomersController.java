package io.licensemanager.backend.restcontroller;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.service.CustomersService;
import io.licensemanager.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping(path = "/api/customers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomersController {

    private static final Logger logger = LoggerFactory.getLogger(CustomersController.class);

    private final CustomersService customersService;

    @GetMapping(path = "/customers_list")
    public ResponseEntity<?> getCustomersList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = AuthenticationUtils.parseUsername(authentication);
        Set<ROLES_PERMISSIONS> permissions = AuthenticationUtils.parsePermissions(authentication);

        return ResponseEntity.ok(
                customersService.getCustomersList(username, permissions)
        );
    }

    @GetMapping(path = "/groups_list")
    public ResponseEntity<?> getCustomersGroupsForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = AuthenticationUtils.parseUsername(authentication);

        return ResponseEntity.ok(
                customersService.getCustomersGroupsList(username)
        );
    }

}
