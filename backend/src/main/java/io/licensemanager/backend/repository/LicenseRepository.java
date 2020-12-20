package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Customer;
import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.LicenseTemplate;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {
    List<License> findAllByCreatorIs(final User creator);

    Optional<License> findByIdAndCreatorIs(final Long id, final User creator);

    boolean existsByUsedTemplateIs(final LicenseTemplate template);

    boolean existsByCustomerIs(final Customer customer);

    List<License> findAllByCustomerId(final Long customerId);

    List<License> findAllByCustomerIdAndCreatorIs(final Long customerId, final User creator);
}
