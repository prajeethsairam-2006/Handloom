package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Order;
import com.handloom.marketplace.model.OrderItem;
import com.handloom.marketplace.model.Payment;
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
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getLong("customer_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setOrderStatus(rs.getString("order_status"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setPhone(rs.getString("phone"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setCity(rs.getString("city"));
        o.setState(rs.getString("state"));
        o.setPinCode(rs.getString("pin_code"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            o.setCreatedAt(ts.toLocalDateTime());
        }

        // Joined payment fields if available
        try {
            Long paymentId = rs.getLong("payment_id");
            if (!rs.wasNull()) {
                Payment payment = new Payment();
                payment.setId(paymentId);
                payment.setOrderId(o.getId());
                payment.setAmount(rs.getBigDecimal("payment_amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setPaymentStatus(rs.getString("payment_status"));
                Timestamp pTs = rs.getTimestamp("payment_date");
                if (pTs != null) {
                    payment.setPaymentDate(pTs.toLocalDateTime());
                }
                o.setPayment(payment);
            }
        } catch (Exception ignored) {}

        return o;
    };

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem oi = new OrderItem();
        oi.setId(rs.getLong("id"));
        oi.setOrderId(rs.getLong("order_id"));
        oi.setProductId(rs.getLong("product_id"));
        oi.setArtisanId(rs.getLong("artisan_id"));
        oi.setProductName(rs.getString("product_name"));
        oi.setUnitPrice(rs.getBigDecimal("unit_price"));
        oi.setQuantity(rs.getInt("quantity"));
        oi.setSubtotal(rs.getBigDecimal("subtotal"));

        try {
            oi.setProductImageUrl(rs.getString("image_url"));
        } catch (Exception ignored) {}
        try {
            oi.setArtisanBusinessName(rs.getString("artisan_business_name"));
        } catch (Exception ignored) {}

        return oi;
    };

    private static final String BASE_ORDER_SELECT =
            "SELECT o.id, o.order_number, o.customer_id, o.total_amount, o.order_status, " +
            "o.customer_name, o.phone, o.delivery_address, o.city, o.state, o.pin_code, o.created_at, " +
            "p.id AS payment_id, p.amount AS payment_amount, p.payment_method, p.payment_status, p.payment_date " +
            "FROM orders o " +
            "LEFT JOIN payments p ON o.id = p.order_id ";

    public Long save(Order order) {
        String sql = "INSERT INTO orders (order_number, customer_id, total_amount, order_status, " +
                     "customer_name, phone, delivery_address, city, state, pin_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getOrderNumber());
            ps.setLong(2, order.getCustomerId());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getOrderStatus() != null ? order.getOrderStatus() : "PLACED");
            ps.setString(5, order.getCustomerName());
            ps.setString(6, order.getPhone());
            ps.setString(7, order.getDeliveryAddress());
            ps.setString(8, order.getCity());
            ps.setString(9, order.getState());
            ps.setString(10, order.getPinCode());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            order.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public Long saveOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, artisan_id, product_name, unit_price, quantity, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, item.getOrderId());
            ps.setLong(2, item.getProductId());
            ps.setLong(3, item.getArtisanId());
            ps.setString(4, item.getProductName());
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setInt(6, item.getQuantity());
            ps.setBigDecimal(7, item.getSubtotal());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            item.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public int updateStatus(Long orderId, String status) {
        String sql = "UPDATE orders SET order_status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, orderId);
    }

    public Optional<Order> findById(Long id) {
        String sql = BASE_ORDER_SELECT + "WHERE o.id = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, orderRowMapper, id);
            if (order != null) {
                order.setItems(findItemsByOrderId(order.getId()));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = BASE_ORDER_SELECT + "WHERE o.order_number = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, orderRowMapper, orderNumber);
            if (order != null) {
                order.setItems(findItemsByOrderId(order.getId()));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Order> findByCustomerId(Long customerId) {
        String sql = BASE_ORDER_SELECT + "WHERE o.customer_id = ? ORDER BY o.id DESC";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, customerId);
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    public List<Order> findByArtisanId(Long artisanId) {
        String sql = "SELECT DISTINCT o.id, o.order_number, o.customer_id, o.total_amount, o.order_status, " +
                "o.customer_name, o.phone, o.delivery_address, o.city, o.state, o.pin_code, o.created_at, " +
                "p.id AS payment_id, p.amount AS payment_amount, p.payment_method, p.payment_status, p.payment_date " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "LEFT JOIN payments p ON o.id = p.order_id " +
                "WHERE oi.artisan_id = ? ORDER BY o.id DESC";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, artisanId);
        for (Order o : orders) {
            o.setItems(findItemsByOrderIdAndArtisan(o.getId(), artisanId));
        }
        return orders;
    }

    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.artisan_id, oi.product_name, " +
                "oi.unit_price, oi.quantity, oi.subtotal, p.image_url, a.business_name AS artisan_business_name " +
                "FROM order_items oi " +
                "LEFT JOIN products p ON oi.product_id = p.id " +
                "LEFT JOIN artisans a ON oi.artisan_id = a.id " +
                "WHERE oi.order_id = ? ORDER BY oi.id ASC";
        return jdbcTemplate.query(sql, orderItemRowMapper, orderId);
    }

    public List<OrderItem> findItemsByOrderIdAndArtisan(Long orderId, Long artisanId) {
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.artisan_id, oi.product_name, " +
                "oi.unit_price, oi.quantity, oi.subtotal, p.image_url, a.business_name AS artisan_business_name " +
                "FROM order_items oi " +
                "LEFT JOIN products p ON oi.product_id = p.id " +
                "LEFT JOIN artisans a ON oi.artisan_id = a.id " +
                "WHERE oi.order_id = ? AND oi.artisan_id = ? ORDER BY oi.id ASC";
        return jdbcTemplate.query(sql, orderItemRowMapper, orderId, artisanId);
    }

    public List<Order> findAll() {
        String sql = BASE_ORDER_SELECT + "ORDER BY o.id DESC";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper);
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    public List<Order> findRecentOrders(int limit) {
        String sql = BASE_ORDER_SELECT + "ORDER BY o.id DESC LIMIT ?";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, limit);
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM orders";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countPendingOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_status IN ('PLACED', 'PROCESSING', 'CONFIRMED')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public BigDecimal getTotalSales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_status != 'CANCELLED'";
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return total != null ? total : BigDecimal.ZERO;
    }

    public int countOrdersByArtisan(Long artisanId) {
        String sql = "SELECT COUNT(DISTINCT order_id) FROM order_items WHERE artisan_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, artisanId);
        return count != null ? count : 0;
    }

    public int countProductsSoldByArtisan(Long artisanId) {
        String sql = "SELECT COALESCE(SUM(oi.quantity), 0) FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE oi.artisan_id = ? AND o.order_status != 'CANCELLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, artisanId);
        return count != null ? count : 0;
    }
}
