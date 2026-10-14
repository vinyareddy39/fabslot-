package com.fabslot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fabslot.dto.RegisterRequest;
import com.fabslot.entity.Member;
import com.fabslot.entity.MemberRole;
import com.fabslot.exception.DuplicateResourceException;
import com.fabslot.exception.InvalidCredentialsException;
import com.fabslot.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fabslot.auth.admin-secret:fabslot-admin-secret-2026}")
    private String adminSecret;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member register(RegisterRequest req) {
        String username = req.getUsername().trim();
        String email = req.getEmail().trim().toLowerCase();

        if (memberRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already taken: " + username);
        }

        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }

        MemberRole role = MemberRole.STUDENT;
        if (req.getRole() != null && !req.getRole().isBlank()) {
            try {
                role = MemberRole.valueOf(req.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role specified. Must be STUDENT or LAB_ADMIN");
            }
        }

        if (role == MemberRole.LAB_ADMIN) {
            if (req.getAdminCode() == null || !req.getAdminCode().trim().equals(adminSecret)) {
                throw new IllegalArgumentException("Invalid admin secret code. Self-registration without valid code is restricted to students.");
            }
        }

        Member member = new Member(
                username,
                passwordEncoder.encode(req.getPassword()),
                email,
                req.getFullName().trim(),
                role
        );

        return memberRepository.save(member);
    }

    public Member authenticate(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return member;
    }

    public Member getByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + username));
    }
}
