package com.handloom.marketplace.repository;

import com.handloom.marketplace.model.Artisan;
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
public class ArtisanRepository {

    private final JdbcTemplate jdbcTemplate;

    public ArtisanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Artisan> artisanRowMapper = (rs, rowNum) -> {
        Artisan a = new Artisan();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setBusinessName(rs.getString("business_name"));
        a.setCraftType(rs.getString("craft_type"));
        a.setLocation(rs.getString("location"));
        a.setDescription(rs.getString("description"));
        a.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }

        // Joined fields if available
        try {
            a.setName(rs.getString("user_name"));
            a.setEmail(rs.getString("user_email"));
            a.setPhone(rs.getString("user_phone"));
        } catch (Exception ignored) {
        }

        return a;
    };

    public Long save(Artisan artisan) {
        String sql = "INSERT INTO artisans (user_id, business_name, craft_type, location, description, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, artisan.getUserId());
            ps.setString(2, artisan.getBusinessName());
            ps.setString(3, artisan.getCraftType());
            ps.setString(4, artisan.getLocation());
            ps.setString(5, artisan.getDescription());
            ps.setString(6, artisan.getStatus() != null ? artisan.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            artisan.setId(key.longValue());
            return key.longValue();
        }
        return null;
    }

    public int update(Artisan artisan) {
        String sql = "UPDATE artisans SET business_name = ?, craft_type = ?, location = ?, description = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                artisan.getBusinessName(),
                artisan.getCraftType(),
                artisan.getLocation(),
                artisan.getDescription(),
                artisan.getStatus(),
                artisan.getId());
    }

    public Optional<Artisan> findById(Long id) {
        String sql = "SELECT a.id, a.user_id, a.business_name, a.craft_type, a.location, a.description, a.status, a.created_at, " +
                "u.name AS user_name, u.email AS user_email, u.phone AS user_phone " +
                "FROM artisans a JOIN users u ON a.user_id = u.id WHERE a.id = ?";
        try {
            Artisan artisan = jdbcTemplate.queryForObject(sql, artisanRowMapper, id);
            return Optional.ofNullable(artisan);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Artisan> findByUserId(Long userId) {
        String sql = "SELECT a.id, a.user_id, a.business_name, a.craft_type, a.location, a.description, a.status, a.created_at, " +
                "u.name AS user_name, u.email AS user_email, u.phone AS user_phone " +
                "FROM artisans a JOIN users u ON a.user_id = u.id WHERE a.user_id = ?";
        try {
            Artisan artisan = jdbcTemplate.queryForObject(sql, artisanRowMapper, userId);
            return Optional.ofNullable(artisan);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Artisan> findAll() {
        String sql = "SELECT a.id, a.user_id, a.business_name, a.craft_type, a.location, a.description, a.status, a.created_at, " +
                "u.name AS user_name, u.email AS user_email, u.phone AS user_phone " +
                "FROM artisans a JOIN users u ON a.user_id = u.id ORDER BY a.id ASC";
        return jdbcTemplate.query(sql, artisanRowMapper);
    }

    public List<Artisan> findAllActive() {
        String sql = "SELECT a.id, a.user_id, a.business_name, a.craft_type, a.location, a.description, a.status, a.created_at, " +
                "u.name AS user_name, u.email AS user_email, u.phone AS user_phone " +
                "FROM artisans a JOIN users u ON a.user_id = u.id WHERE a.status = 'ACTIVE' ORDER BY a.id ASC";
        return jdbcTemplate.query(sql, artisanRowMapper);
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM artisans";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
