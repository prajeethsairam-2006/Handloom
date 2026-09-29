package com.handloom.marketplace.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Long artisanId;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String material;
    private String originLocation;
    private String status; // 'ACTIVE', 'INACTIVE'
    private LocalDateTime createdAt;

    // Joined fields for display
    private String categoryName;
    private String artisanBusinessName;
    private String artisanLocation;
    private Double averageRating;
    private Integer reviewCount;

    public Product() {
    }

    public Product(Long id, String name, String description, Long categoryId, Long artisanId, BigDecimal price,
                   Integer stockQuantity, String imageUrl, String material, String originLocation, String status,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.artisanId = artisanId;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.material = material;
        this.originLocation = originLocation;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getArtisanId() {
        return artisanId;
    }

    public void setArtisanId(Long artisanId) {
        this.artisanId = artisanId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getArtisanBusinessName() {
        return artisanBusinessName;
    }

    public void setArtisanBusinessName(String artisanBusinessName) {
        this.artisanBusinessName = artisanBusinessName;
    }

    public String getArtisanLocation() {
        return artisanLocation;
    }

    public void setArtisanLocation(String artisanLocation) {
        this.artisanLocation = artisanLocation;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }
}
