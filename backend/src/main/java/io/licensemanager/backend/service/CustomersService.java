package io.licensemanager.backend.service;

import io.licensemanager.backend.configuration.setup.ROLES_PERMISSIONS;
import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.CustomerGroup;
import io.licensemanager.backend.entity.User;
import io.licensemanager.backend.repository.CustomerGroupRepository;
import io.licensemanager.backend.repository.CustomerRepository;
import io.licensemanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomersService {

    private static final Logger logger = LoggerFactory.getLogger(CustomersService.class);

    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final UserRepository userRepository;

    private List<Customer> filterCustomersGroups(List<Customer> customers, final User creator) {
        return customers.stream()
                .map(customer -> {
                    Set<CustomerGroup> groupsOwnedByUser = customer.getGroups().stream()
                            .filter(group -> group.getCreator().equals(creator))
                            .collect(Collectors.toSet());
                    customer.setGroups(groupsOwnedByUser);
                    return customer;
                })
                .collect(Collectors.toList());
    }

    public List<Customer> getCustomersList(final String username, final Set<ROLES_PERMISSIONS> userPermissions) {
        logger.debug("Getting customers list");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.error("Cannot find user which requested customers list");
            return Collections.emptyList();
        }
        User creator = user.get();
        if (userPermissions.contains(ROLES_PERMISSIONS.VIEW_ALL_CUSTOMERS)
                || userPermissions.contains(ROLES_PERMISSIONS.ALL)) {
            return filterCustomersGroups(customerRepository.findAll(), creator);
        }

        return filterCustomersGroups(customerRepository.findAllByCreatorIs(creator), creator);
    }

    public List<CustomerGroup> getCustomersGroupsList(final String username) {
        logger.debug("Getting customer groups list");
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return customerGroupRepository.findAllByCreatorIs(user.get());
        }

        return Collections.emptyList();
    }

}
