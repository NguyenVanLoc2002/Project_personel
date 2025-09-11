package com.fit.project_personel.controllers;

import com.fit.project_personel.dtos.reponses.auth.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public MeResponse getMe(Authentication authentication) {
        var user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        var email = user.getUsername(); // Assuming username is the email

        return new MeResponse(null, email);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('ADMIN_DASHBOARD')")
    public String checkRoleAdmin() {
        return "Just Admin has permission ADMIN_DASHBOARD to access this endpoint!";
    }
}
