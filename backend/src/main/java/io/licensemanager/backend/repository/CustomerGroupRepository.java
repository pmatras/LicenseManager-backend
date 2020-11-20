package io.licensemanager.backend.repository;

import io.licensemanager.backend.entity.CustomerGroup;
import io.licensemanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {

    List<CustomerGroup> findAllByCreatorIs(final User creator);
}