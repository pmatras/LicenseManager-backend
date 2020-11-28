package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.LicenseTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseTemplateRepository extends JpaRepository<LicenseTemplate, Long> {
}
