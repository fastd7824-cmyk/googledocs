package com.app.dao;

import com.app.config.DatabaseConfig;
import com.app.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Roles.
 */
public class RoleDAO {

    /**
     * Find role by ID.
     */
    public Role findById(int id) throws SQLException {
        String sql = "SELECT * FROM roles WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRole(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find role by name.
     */
    public Role findByName(String name) throws SQLException {
        String sql = "SELECT * FROM roles WHERE name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRole(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all roles.
     */
    public List<Role> findAll() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(extractRole(rs));
            }
        }
        return roles;
    }

    private Role extractRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getInt("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        return role;
    }
}