package com.fabslot.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fabslot.entity.Member;
import com.fabslot.entity.MemberRole;
import com.fabslot.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member register(String username, String rawPassword, String roleStr) {
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        MemberRole role = MemberRole.STUDENT;
        if (roleStr != null && !roleStr.isBlank()) {
            role = MemberRole.valueOf(roleStr.toUpperCase());
        }

        Member member = new Member(username, passwordEncoder.encode(rawPassword), role);
        return memberRepository.save(member);
    }

    public Member authenticate(String username, String rawPassword) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return member;
    }
}
