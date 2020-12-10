package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.EmailAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailAlertRepository extends JpaRepository<EmailAlert, Long> {
    Optional<EmailAlert> findByUserId(final Long userId);
}
