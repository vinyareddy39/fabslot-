package com.fabslot.dto;

import com.fabslot.entity.Member;

public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;

    public UserDto() {
    }

    public UserDto(Long id, String username, String email, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static UserDto fromEntity(Member member) {
        if (member == null) {
            return null;
        }
        return new UserDto(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getFullName(),
                member.getRole() != null ? member.getRole().name() : null
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
