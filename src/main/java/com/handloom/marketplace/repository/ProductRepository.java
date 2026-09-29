package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setArtisanId(rs.getLong("artisan_id"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setMaterial(rs.getString("material"));
        p.setOriginLocation(rs.getString("origin_location"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }

        // Joined fields if present
        try {
            p.setCategoryName(rs.getString("category_name"));
        } catch (Exception ignored) {}
        try {
            p.setArtisanBusinessName(rs.getString("artisan_business_name"));
        } catch (Exception ignored) {}
        try {
            p.setArtisanLocation(rs.getString("artisan_location"));
        } catch (Exception ignored) {}
        try {
            double avg = rs.getDouble("avg_rating");
            p.setAverageRating(rs.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0);
        } catch (Exception ignored) {}
        try {
            p.setReviewCount(rs.getInt("review_count"));
        } catch (Exception ignored) {}

        return p;
    };

    private static final String BASE_SELECT =
            "SELECT p.id, p.name, p.description, p.category_id, p.artisan_id, p.price, p.stock_quantity, " +
            "p.image_url, p.material, p.origin_location, p.status, p.created_at, " +
            "c.name AS category_name, a.business_name AS artisan_business_name, a.location AS artisan_location, " +
            "COALESCE(AVG(r.rating), 0.0) AS avg_rating, COUNT(r.id) AS review_count " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "LEFT JOIN artisans a ON p.artisan_id = a.id " +
            "LEFT JOIN reviews r ON p.id = r.product_id ";

    public Long save(Product product) {
        String sql = "INSERT INTO products (name, description, category_id, artisan_id, price, stock_quantity, image_url, material, origin_location, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setLong(3, product.getCategoryId());
            ps.setLong(4, product.getArtisanId());
            ps.setBigDecimal(5, product.getPrice());
            ps.setInt(6, product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            ps.setString(7, product.getImageUrl());
            ps.setString(8, product.getMaterial());
            ps.setString(9, product.getOriginLocation());
            ps.setString(10, product.getStatus() != null ? product.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            product.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public int update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, category_id = ?, price = ?, stock_quantity = ?, " +
                     "image_url = ?, material = ?, origin_location = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getMaterial(),
                product.getOriginLocation(),
                product.getStatus(),
                product.getId());
    }

    public int updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newStock, productId);
    }

    public int reduceStock(Long productId, int quantity) {
        // Enforce in SQL as well that stock cannot go below zero
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        return jdbcTemplate.update(sql, quantity, productId, quantity);
    }

    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ? GROUP BY p.id, c.name, a.business_name, a.location";
        try {
            Product p = jdbcTemplate.queryForObject(sql, productRowMapper, id);
            return Optional.ofNullable(p);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Product> findAll() {
        String sql = BASE_SELECT + "GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public List<Product> findAllActive() {
        String sql = BASE_SELECT + "WHERE p.status = 'ACTIVE' GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    public List<Product> findByArtisanId(Long artisanId) {
        String sql = BASE_SELECT + "WHERE p.artisan_id = ? GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, artisanId);
    }

    public List<Product> findByCategory(Long categoryId) {
        String sql = BASE_SELECT + "WHERE p.category_id = ? AND p.status = 'ACTIVE' GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, productRowMapper, categoryId);
    }

    public List<Product> findFeatured(int limit) {
        String sql = BASE_SELECT + "WHERE p.status = 'ACTIVE' GROUP BY p.id, c.name, a.business_name, a.location ORDER BY avg_rating DESC, p.id DESC LIMIT ?";
        return jdbcTemplate.query(sql, productRowMapper, limit);
    }

    public List<Product> findNewProducts(int limit) {
        String sql = BASE_SELECT + "WHERE p.status = 'ACTIVE' GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, productRowMapper, limit);
    }

    public List<Product> findLowStock(int threshold) {
        String sql = BASE_SELECT + "WHERE p.stock_quantity <= ? GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.stock_quantity ASC";
        return jdbcTemplate.query(sql, productRowMapper, threshold);
    }

    public List<Product> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE p.status = 'ACTIVE' ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(p.material) LIKE ? OR LOWER(p.origin_location) LIKE ?) ");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        sql.append("GROUP BY p.id, c.name, a.business_name, a.location ORDER BY p.id DESC");
        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countByArtisan(Long artisanId) {
        String sql = "SELECT COUNT(*) FROM products WHERE artisan_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, artisanId);
        return count != null ? count : 0;
    }

    public int countInStockByArtisan(Long artisanId) {
        String sql = "SELECT COUNT(*) FROM products WHERE artisan_id = ? AND stock_quantity > 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, artisanId);
        return count != null ? count : 0;
    }

    public int countLowStockByArtisan(Long artisanId, int threshold) {
        String sql = "SELECT COUNT(*) FROM products WHERE artisan_id = ? AND stock_quantity <= ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, artisanId, threshold);
        return count != null ? count : 0;
    }
}
