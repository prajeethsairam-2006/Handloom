package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Cart;
import com.handloom.marketplace.model.CartItem;
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
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Cart> cartRowMapper = (rs, rowNum) -> {
        Cart cart = new Cart();
        cart.setId(rs.getLong("id"));
        cart.setCustomerId(rs.getLong("customer_id"));
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) {
            cart.setUpdatedAt(ts.toLocalDateTime());
        }
        return cart;
    };

    private final RowMapper<CartItem> cartItemRowMapper = (rs, rowNum) -> {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setCartId(rs.getLong("cart_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setProductName(rs.getString("product_name"));
        item.setProductPrice(rs.getBigDecimal("product_price"));
        item.setProductImageUrl(rs.getString("image_url"));
        item.setAvailableStock(rs.getInt("stock_quantity"));
        item.setArtisanBusinessName(rs.getString("artisan_business_name"));
        return item;
    };

    public Cart findOrCreateCart(Long customerId) {
        String selectSql = "SELECT id, customer_id, updated_at FROM carts WHERE customer_id = ?";
        try {
            return jdbcTemplate.queryForObject(selectSql, cartRowMapper, customerId);
        } catch (EmptyResultDataAccessException e) {
            String insertSql = "INSERT INTO carts (customer_id) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, customerId);
                return ps;
            }, keyHolder);

            Cart cart = new Cart();
            if (keyHolder.getKey() != null) {
                cart.setId(keyHolder.getKey().longValue());
            }
            cart.setCustomerId(customerId);
            return cart;
        }
    }

    public List<CartItem> findItemsByCartId(Long cartId) {
        String sql = "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, " +
                "p.name AS product_name, p.price AS product_price, p.image_url, p.stock_quantity, " +
                "a.business_name AS artisan_business_name " +
                "FROM cart_items ci " +
                "JOIN products p ON ci.product_id = p.id " +
                "LEFT JOIN artisans a ON p.artisan_id = a.id " +
                "WHERE ci.cart_id = ? ORDER BY ci.id ASC";
        return jdbcTemplate.query(sql, cartItemRowMapper, cartId);
    }

    public Optional<CartItem> findCartItem(Long cartId, Long productId) {
        String sql = "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, " +
                "p.name AS product_name, p.price AS product_price, p.image_url, p.stock_quantity, " +
                "a.business_name AS artisan_business_name " +
                "FROM cart_items ci " +
                "JOIN products p ON ci.product_id = p.id " +
                "LEFT JOIN artisans a ON p.artisan_id = a.id " +
                "WHERE ci.cart_id = ? AND ci.product_id = ?";
        try {
            CartItem item = jdbcTemplate.queryForObject(sql, cartItemRowMapper, cartId, productId);
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void addItem(Long cartId, Long productId, int quantity) {
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, cartId, productId, quantity);
    }

    public int updateItemQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        return jdbcTemplate.update(sql, quantity, cartItemId);
    }

    public int removeItem(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        return jdbcTemplate.update(sql, cartItemId);
    }

    public int clearCart(Long cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        return jdbcTemplate.update(sql, cartId);
    }

    public int countItems(Long cartId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE cart_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cartId);
        return count != null ? count : 0;
    }
}
