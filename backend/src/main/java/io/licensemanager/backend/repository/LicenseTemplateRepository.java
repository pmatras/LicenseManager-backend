package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.LicenseTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicenseTemplateRepository extends JpaRepository<LicenseTemplate, Long> {
    Optional<LicenseTemplate> findByName(final String templateName);
}
