package com.project.coalba.domain.auth.controller;

import com.project.coalba.domain.auth.entity.enums.Provider;
import com.project.coalba.domain.auth.entity.enums.Role;
import com.project.coalba.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam Provider provider, @RequestParam String token, @RequestParam Role role) {
        authService.login(provider, token, role);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh() {
        authService.reissue();
        return ResponseEntity.ok().build();
    }
}
