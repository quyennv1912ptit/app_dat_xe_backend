package com.nhom15.app_dat_xe.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table (name = "drivers")
public class Drivers {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (name = "phone_number", nullable = false)
    private String phoneNumber;
    @Column (name = "firebase_uid", unique = true)
    private String firebaseUid;
    @Column (name = "email")
    private String email;
    @Column (name = "full_name")
    private String fullName;
    @Column (name = "national_id")
    private String nationalId;
    @Column (name = "license_number")
    private String licenseNumber;
    @Column (name = "license_expiry_date")
    private LocalDate licenseExpiryDate;
    @Column (name = "license_verified")
    private Boolean licenseVerified;
    @Column (name = "current_vehicle_id")
    private Long currentVehicleId;
    @Column (name = "status")
    private String status;
    @Column (name = "online_status")
    private String onlineStatus;
    @Column (name = "current_lat")
    private Double currentLat;
    @Column (name = "current_lng")
    private Double currentLng;
    @Column (name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
    @Column (name = "average_rating")
    private Double averageRating;
    @Column (name = "total_trips")
    private int totalTrips;
    @Column (name = "completion_rate")
    private Double completionRate;
    @Column (name = "cancel_rate_30d")
    private Double cancelRate30d;
    @Column (name = "acceptance_rate")
    private Double acceptanceRate;
    @Column (name = "bank_account_number")
    private String bankAccountNumber;
    @Column (name = "wallet_balance", precision = 15, scale = 2)
    private BigDecimal walletBalance;
    @Column (name = "pending_payout", precision = 15, scale = 2)
    private BigDecimal pendingPayout;
    @Column (name = "device_id")
    private String deviceId;
    @Column (name = "risk_score")
    private Double riskScore;
    @Column (name = "is_flagged")
    private Boolean isFlagged;
    @Column (name = "gps_spoofing_count")
    private int gpsSpoofingCount;
    @Column (name = "multi_account_flag")
    private Boolean multiAccountFlag;
    @Column (name = "background_check_status")
    private String backgroundCheckStatus;
    @Column (name = "created_at")
    private LocalDateTime createdAt;
    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

    public Drivers() {
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public Boolean getFlagged() {
        return isFlagged;
    }

    public void setFlagged(Boolean flagged) {
        isFlagged = flagged;
    }

    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) {
        this.licenseExpiryDate = licenseExpiryDate;
    }

    public Boolean getLicenseVerified() {
        return licenseVerified;
    }

    public void setLicenseVerified(Boolean licenseVerified) {
        this.licenseVerified = licenseVerified;
    }

    public Long getCurrentVehicleId() {
        return currentVehicleId;
    }

    public void setCurrentVehicleId(Long currentVehicleId) {
        this.currentVehicleId = currentVehicleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Double getCancelRate30d() {
        return cancelRate30d;
    }

    public void setCancelRate30d(Double cancelRate30d) {
        this.cancelRate30d = cancelRate30d;
    }

    public Double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(Double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public BigDecimal getPendingPayout() {
        return pendingPayout;
    }

    public void setPendingPayout(BigDecimal pendingPayout) {
        this.pendingPayout = pendingPayout;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Boolean getIsFlagged() {
        return isFlagged;
    }

    public void setIsFlagged(Boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public int getGpsSpoofingCount() {
        return gpsSpoofingCount;
    }

    public void setGpsSpoofingCount(int gpsSpoofingCount) {
        this.gpsSpoofingCount = gpsSpoofingCount;
    }

    public Boolean getMultiAccountFlag() {
        return multiAccountFlag;
    }

    public void setMultiAccountFlag(Boolean multiAccountFlag) {
        this.multiAccountFlag = multiAccountFlag;
    }

    public String getBackgroundCheckStatus() {
        return backgroundCheckStatus;
    }

    public void setBackgroundCheckStatus(String backgroundCheckStatus) {
        this.backgroundCheckStatus = backgroundCheckStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
