package com.handloom.marketplace.model;

import java.time.LocalDateTime;

public class Artisan {
    private Long id;
    private Long userId;
    private String businessName;
    private String craftType;
    private String location;
    private String description;
    private String status; // 'ACTIVE', 'INACTIVE'
    private LocalDateTime createdAt;

    // Joined User fields for easy display
    private String name;
    private String email;
    private String phone;

    public Artisan() {
    }

    public Artisan(Long id, Long userId, String businessName, String craftType, String location, String description, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.craftType = craftType;
        this.location = location;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCraftType() {
        return craftType;
    }

    public void setCraftType(String craftType) {
        this.craftType = craftType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
