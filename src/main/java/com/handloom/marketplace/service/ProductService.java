package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.Product;
import com.handloom.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllActive() {
        return productRepository.findAllActive();
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategory(categoryId);
    }

    public List<Product> findByArtisanId(Long artisanId) {
        return productRepository.findByArtisanId(artisanId);
    }

    public List<Product> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.search(keyword, categoryId, minPrice, maxPrice);
    }

    public List<Product> findFeatured(int limit) {
        return productRepository.findFeatured(limit);
    }

    public List<Product> findNewProducts(int limit) {
        return productRepository.findNewProducts(limit);
    }

    public List<Product> findLowStock(int threshold) {
        return productRepository.findLowStock(threshold);
    }

    public Long save(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public void update(Product product) {
        validateProduct(product);
        productRepository.update(product);
    }

    public void updateStock(Long productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        productRepository.updateStock(productId, newStock);
    }

    public void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("Category selection is required.");
        }
    }

    public int countAll() {
        return productRepository.countAll();
    }

    public int countByArtisan(Long artisanId) {
        return productRepository.countByArtisan(artisanId);
    }

    public int countInStockByArtisan(Long artisanId) {
        return productRepository.countInStockByArtisan(artisanId);
    }

    public int countLowStockByArtisan(Long artisanId, int threshold) {
        return productRepository.countLowStockByArtisan(artisanId, threshold);
    }
}
