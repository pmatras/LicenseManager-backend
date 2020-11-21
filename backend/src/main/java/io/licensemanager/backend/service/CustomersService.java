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

import java.time.LocalDateTime;
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

    private List<Customer> checkPermissionsToCustomers(List<Customer> customers, User creator,
                                                       Set<ROLES_PERMISSIONS> userPermissions) {
        logger.debug("Checking permissions to all requested customers!");
        if (userPermissions.contains(ROLES_PERMISSIONS.ALL) ||
                userPermissions.contains(ROLES_PERMISSIONS.EDIT_ALL_CUSTOMERS)) {
            return customers;
        }
        logger.error("User doesn't have permissions to all requested customers - skipping forbidden customers");

        return customers.stream()
                .filter(record -> record.getCreator().equals(creator))
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

    public Optional<Customer> createCustomerIfNotExists(final String customerName, final Set<String> groups,
                                                        final String creatorsUsername) {
        logger.debug("Creating new customer: {}", customerName);
        Optional<User> creator = userRepository.findByUsername(creatorsUsername);
        if (creator.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return Optional.empty();
        }
        Optional<Customer> existingCustomer = customerRepository.findByCreatorIsAndName(creator.get(), customerName);
        if (existingCustomer.isEmpty()) {
            Customer customer = new Customer();
            customer.setName(customerName);
            customer.setCreationDate(LocalDateTime.now());
            customer.setCreator(creator.get());
            if (groups != null && !groups.isEmpty()) {
                logger.debug("Assigning customer to requested groups");
                Set<CustomerGroup> customerGroups = customerGroupRepository.findAllByCreatorIsAndNameIn(creator.get(), groups);
                customer.setGroups(customerGroups);
            }

            return Optional.of(customerRepository.save(customer));
        }
        logger.error("Error - customer with this name already exists for this user");

        return Optional.empty();
    }

    public Optional<CustomerGroup> createGroupIfNotExists(final String groupName, final String displayColor,
                                                          final Set<Long> customersIds, final String creatorUsername,
                                                          final Set<ROLES_PERMISSIONS> permissions) {
        logger.debug("Creating new group: {}", groupName);
        Optional<User> creator = userRepository.findByUsername(creatorUsername);
        if (creator.isEmpty()) {
            logger.error("Cannot find user with passed username");
            return Optional.empty();
        }
        Optional<CustomerGroup> existingGroup = customerGroupRepository.findByCreatorIsAndName(creator.get(), groupName);
        if (existingGroup.isEmpty()) {
            CustomerGroup group = new CustomerGroup();
            group.setName(groupName);
            group.setDisplayColor(displayColor);
            group.setCreator(creator.get());
            CustomerGroup createdGroup = customerGroupRepository.save(group);
            if (customersIds != null && !customersIds.isEmpty()) {
                logger.debug("Assigning customers to requested group");
                List<Customer> customersList = checkPermissionsToCustomers(
                        customerRepository.findAllById(customersIds),
                        creator.get(),
                        permissions
                );

                customersList.forEach(customer -> {
                    Set<CustomerGroup> assignedGroups = customer.getGroups();
                    assignedGroups.add(createdGroup);
                    customer.setGroups(assignedGroups);
                });

                customerRepository.saveAll(customersList);
            }

            return Optional.of(createdGroup);
        }
        logger.error("Error - customer with this name already exists for this user");

        return Optional.empty();
    }

}
