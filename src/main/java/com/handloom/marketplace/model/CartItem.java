package com.handloom.marketplace.model;

import java.math.BigDecimal;

public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;

    // Joined fields for display
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private Integer availableStock;
    private String artisanBusinessName;
    private BigDecimal subtotal;

    public CartItem() {
    }

    public CartItem(Long id, Long cartId, Long productId, Integer quantity) {
        this.id = id;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        updateSubtotal();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
        updateSubtotal();
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getArtisanBusinessName() {
        return artisanBusinessName;
    }

    public void setArtisanBusinessName(String artisanBusinessName) {
        this.artisanBusinessName = artisanBusinessName;
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null) {
            updateSubtotal();
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    private void updateSubtotal() {
        if (productPrice != null && quantity != null) {
            this.subtotal = productPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
