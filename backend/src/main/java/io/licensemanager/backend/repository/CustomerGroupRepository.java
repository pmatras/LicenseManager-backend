package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
}