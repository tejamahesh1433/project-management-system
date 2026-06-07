package com.projectmanagementsaas.workspace.repository;

import com.projectmanagementsaas.workspace.entity.Organization;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsBySlugIgnoreCase(String slug);

    List<Organization> findByOwner_Id(UUID ownerId);
}
