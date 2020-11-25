package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.CustomerGroup;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCreatorIsAndName(final User creator, final String name);

    Optional<Customer> findByCreatorIsAndId(final User creator, final Long customerId);

    List<Customer> findAllByCreatorIs(final User creator);

    List<Customer> findAllByGroupsContains(final CustomerGroup group);
}
