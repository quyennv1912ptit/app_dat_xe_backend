package com.nhom15.app_dat_xe.dto;

public class LoginRequest {
    private String idToken;
    private String role;

    public LoginRequest() {
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
