package com.example.auth.dto;

import com.example.auth.entity.User;

public class AuthResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String phno;

    public AuthResponse() {}

    public AuthResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phno = user.getPhno();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhno() { return phno; }
    public void setPhno(String phno) { this.phno = phno; }
}
