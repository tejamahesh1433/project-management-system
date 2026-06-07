package com.projectmanagementsaas.role.repository;

import com.projectmanagementsaas.role.entity.Role;
import com.projectmanagementsaas.role.entity.RoleName;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}
