package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByCreatorIs(final User creator);
}
