package com.fit.project_personel.services;

import com.fit.project_personel.models.Permission;
import com.fit.project_personel.models.Role;
import com.fit.project_personel.models.User;
import com.fit.project_personel.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        var authorities = user.getRoles().stream()
                .flatMap((Role r) -> {
                    var roleAuth = new SimpleGrantedAuthority("ROLE_" + r.getName());

                    var permAuths = r.getPermissions().stream()
                            .map(Permission::getName)
                            .map(Enum::name)
                            .map(SimpleGrantedAuthority::new);
                    return Stream.concat(Stream.of(roleAuth), permAuths);
                })
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(Boolean.FALSE.equals(user.getActive()))
                .build();


    }

}
