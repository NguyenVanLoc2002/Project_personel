package com.fit.project_personel.repositories;

import com.fit.project_personel.enums.PermissionName;
import com.fit.project_personel.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(PermissionName name);
}
