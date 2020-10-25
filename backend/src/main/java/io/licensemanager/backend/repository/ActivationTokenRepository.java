package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByValue(final String tokenValue);
    Optional<ActivationToken> findByUserId(final Long userId);
}
