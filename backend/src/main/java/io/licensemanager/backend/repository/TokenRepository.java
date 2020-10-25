package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByValue(final String tokenValue);

    Optional<Token> findByUserId(final Long userId);
}
