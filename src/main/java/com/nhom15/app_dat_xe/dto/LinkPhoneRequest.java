package com.nhom15.app_dat_xe.dto;

public class LinkPhoneRequest {

    private String providerIdToken;
    private String phoneIdToken;
    private String phoneNumber;
    private String role;

    public LinkPhoneRequest() {
    }

    public String getProviderIdToken() {
        return providerIdToken;
    }

    public void setProviderIdToken(String providerIdToken) {
        this.providerIdToken = providerIdToken;
    }

    public String getPhoneIdToken() {
        return phoneIdToken;
    }

    public void setPhoneIdToken(String phoneIdToken) {
        this.phoneIdToken = phoneIdToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}