package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Category;
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
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    };

    public Long save(Category category) {
        String sql = "INSERT INTO categories (name, description, image_url) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getImageUrl());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            category.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public int update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, image_url = ? WHERE id = ?";
        return jdbcTemplate.update(sql, category.getName(), category.getDescription(), category.getImageUrl(), category.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT id, name, description, image_url, created_at FROM categories WHERE id = ?";
        try {
            Category c = jdbcTemplate.queryForObject(sql, categoryRowMapper, id);
            return Optional.ofNullable(c);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, description, image_url, created_at FROM categories WHERE name = ?";
        try {
            Category c = jdbcTemplate.queryForObject(sql, categoryRowMapper, name);
            return Optional.ofNullable(c);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Category> findAll() {
        String sql = "SELECT id, name, description, image_url, created_at FROM categories ORDER BY id ASC";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM categories";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
