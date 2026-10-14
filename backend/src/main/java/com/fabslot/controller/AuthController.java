package com.fabslot.controller;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabslot.dto.AuthResponse;
import com.fabslot.dto.LoginRequest;
import com.fabslot.dto.RegisterRequest;
import com.fabslot.dto.UserDto;
import com.fabslot.entity.Member;
import com.fabslot.security.JwtUtil;
import com.fabslot.security.TokenBlacklistService;
import com.fabslot.service.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(MemberService memberService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        Member member = memberService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromEntity(member));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Member member = memberService.authenticate(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(member.getUsername(), member.getRole().name());

        AuthResponse res = new AuthResponse(
                token,
                member.getId(),
                member.getUsername(),
                member.getFullName(),
                member.getRole().name()
        );

        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                try {
                    Date expiry = jwtUtil.extractExpiration(token);
                    tokenBlacklistService.blacklistToken(token, expiry != null ? expiry.getTime() : System.currentTimeMillis() + 86400000);
                } catch (Exception ignored) {
                    // Ignore parsing errors and return 200
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Member member = memberService.getByUsername(principal.getName());
        return ResponseEntity.ok(UserDto.fromEntity(member));
    }
}
