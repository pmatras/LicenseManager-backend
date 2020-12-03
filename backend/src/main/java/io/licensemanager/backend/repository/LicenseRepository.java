package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.License;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicenseRepository extends JpaRepository<License, Long> {
    List<License> findAllByCreatorIs(final User creator);
}
