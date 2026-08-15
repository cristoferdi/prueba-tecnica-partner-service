package com.telco.backend.service;

import com.telco.backend.domain.Role;
import com.telco.backend.security.jwt.JwtService;
import com.telco.backend.security.user.CustomUserDetails;
import com.telco.backend.web.dto.LoginRequest;
import com.telco.backend.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        String token = jwtService.generateToken(customUserDetails.getUsername(), customUserDetails.getRole().name());

        return new LoginResponse(token, customUserDetails.getRole());
    }
}