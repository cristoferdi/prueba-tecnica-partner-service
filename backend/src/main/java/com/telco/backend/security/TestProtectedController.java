package com.telco.backend.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestProtectedController {

    @GetMapping("/protected")
    @PreAuthorize("isAuthenticated()")
    public String protectedEndpoint() {
        return "protected content";
    }

    @GetMapping("/agente")
    @PreAuthorize("hasRole('AGENTE')")
    public String agenteOnly() {
        return "agente content";
    }
}