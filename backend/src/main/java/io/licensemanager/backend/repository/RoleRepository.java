package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(final String name);

   Set<Role> findAllByNameIn(final Set<String> names);
}
