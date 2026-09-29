package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Payment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setOrderId(rs.getLong("order_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentStatus(rs.getString("payment_status"));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) {
            p.setPaymentDate(ts.toLocalDateTime());
        }
        try {
            p.setOrderNumber(rs.getString("order_number"));
        } catch (Exception ignored) {}
        return p;
    };

    public Long save(Payment payment) {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, payment_status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, payment.getOrderId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "PENDING");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            payment.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public int updateStatus(Long paymentId, String status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, paymentId);
    }

    public int updateStatusByOrderId(Long orderId, String status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE order_id = ?";
        return jdbcTemplate.update(sql, status, orderId);
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        String sql = "SELECT p.id, p.order_id, p.amount, p.payment_method, p.payment_status, p.payment_date, " +
                "o.order_number " +
                "FROM payments p JOIN orders o ON p.order_id = o.id WHERE p.order_id = ?";
        try {
            Payment payment = jdbcTemplate.queryForObject(sql, paymentRowMapper, orderId);
            return Optional.ofNullable(payment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Payment> findAll() {
        String sql = "SELECT p.id, p.order_id, p.amount, p.payment_method, p.payment_status, p.payment_date, " +
                "o.order_number " +
                "FROM payments p JOIN orders o ON p.order_id = o.id ORDER BY p.id DESC";
        return jdbcTemplate.query(sql, paymentRowMapper);
    }
}
