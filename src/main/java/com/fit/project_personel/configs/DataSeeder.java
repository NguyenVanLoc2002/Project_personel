package com.fit.project_personel.configs;

import com.fit.project_personel.enums.PermissionName;
import com.fit.project_personel.models.Permission;
import com.fit.project_personel.models.Role;
import com.fit.project_personel.repositories.PermissionRepository;
import com.fit.project_personel.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        for(PermissionName p : PermissionName.values()){
            permissionRepository.findByName(p).orElseGet(() ->
                permissionRepository.save(Permission.builder()
                        .name(p)
                        .description("Auto generated permission for " + p.name())
                        .build())
            );
        }

        var adminPermissions = Set.of(
                permissionRepository.findByName(PermissionName.USER_READ).orElseThrow(),
                permissionRepository.findByName(PermissionName.USER_CREATE).orElseThrow(),
                permissionRepository.findByName(PermissionName.USER_UPDATE).orElseThrow(),
                permissionRepository.findByName(PermissionName.USER_DELETE).orElseThrow(),
                permissionRepository.findByName(PermissionName.ADMIN_DASHBOARD).orElseThrow()
        );
        roleRepository.findByName("ADMIN").orElseGet(() ->
             roleRepository.save(
                    Role.builder()
                            .name("ADMIN")
                            .description("Administrator with full permissions")
                            .permissions(adminPermissions)
                            .build()
            )
        );

        var userPermissions = Set.of(
                permissionRepository.findByName(PermissionName.USER_READ).orElseThrow()
        );

        roleRepository.findByName("USER").orElseGet(() ->
             roleRepository.save(
                    Role.builder()
                            .name("USER")
                            .description("Default user role with limited permissions")
                            .permissions(userPermissions)
                            .build()
            )
        );
    }
}
