package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCreatorIsAndName(final User creator, final String name);

    List<Customer> findAllByCreatorIs(final User creator);
}
