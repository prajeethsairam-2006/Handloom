package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setProductId(rs.getLong("product_id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("review_date");
        if (ts != null) {
            r.setReviewDate(ts.toLocalDateTime());
        }
        try {
            r.setCustomerName(rs.getString("customer_name"));
        } catch (Exception ignored) {}
        try {
            r.setProductName(rs.getString("product_name"));
        } catch (Exception ignored) {}
        return r;
    };

    public Long save(Review review) {
        String sql = "INSERT INTO reviews (product_id, customer_id, rating, comment) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getCustomerId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            review.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public List<Review> findByProductId(Long productId) {
        String sql = "SELECT r.id, r.product_id, r.customer_id, r.rating, r.comment, r.review_date, " +
                "u.name AS customer_name, p.name AS product_name " +
                "FROM reviews r " +
                "JOIN users u ON r.customer_id = u.id " +
                "JOIN products p ON r.product_id = p.id " +
                "WHERE r.product_id = ? ORDER BY r.id DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, productId);
    }

    public List<Review> findByCustomerId(Long customerId) {
        String sql = "SELECT r.id, r.product_id, r.customer_id, r.rating, r.comment, r.review_date, " +
                "u.name AS customer_name, p.name AS product_name " +
                "FROM reviews r " +
                "JOIN users u ON r.customer_id = u.id " +
                "JOIN products p ON r.product_id = p.id " +
                "WHERE r.customer_id = ? ORDER BY r.id DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, customerId);
    }

    public boolean existsByProductAndCustomer(Long productId, Long customerId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ? AND customer_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId, customerId);
        return count != null && count > 0;
    }

    public Double getAverageRating(Long productId) {
        String sql = "SELECT COALESCE(AVG(rating), 0.0) FROM reviews WHERE product_id = ?";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public int countByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId);
        return count != null ? count : 0;
    }
}
