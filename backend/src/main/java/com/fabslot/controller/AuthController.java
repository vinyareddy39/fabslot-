package com.fabslot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabslot.dto.ApiResponse;
import com.fabslot.dto.AuthResponse;
import com.fabslot.dto.LoginRequest;
import com.fabslot.dto.RegisterRequest;
import com.fabslot.entity.Member;
import com.fabslot.security.JwtUtil;
import com.fabslot.service.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    public AuthController(MemberService memberService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest req) {
        try {
            Member member = memberService.register(req.getUsername(), req.getPassword(), req.getRole());
            return ResponseEntity.ok(ApiResponse.ok("Registered successfully", member.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Member member = memberService.authenticate(req.getUsername(), req.getPassword());
            String token = jwtUtil.generateToken(member.getUsername(), member.getRole().name());
            return ResponseEntity.ok(new AuthResponse(token, member.getUsername(), member.getRole().name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponse.fail(e.getMessage()));
        }
    }
}
