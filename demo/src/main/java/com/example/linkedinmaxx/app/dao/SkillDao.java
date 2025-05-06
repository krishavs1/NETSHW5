package com.example.linkedinmaxx.app.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for inserting and querying user skills in the database.
 */
public class SkillDao {

    /**
     * Saves a single skill for the given user.
     *
     * @param userId the user’s database ID
     * @param skill  the skill text to save
     */
    public void save(int userId, String skill) {
        String sql = "INSERT INTO skills (user_id, skill) VALUES (?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, skill);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving skill", e);
        }
    }

    /**
     * Retrieves all skills for the given user.
     *
     * @param userId the user’s database ID
     * @return a List of skill strings (in insertion order)
     * @throws SQLException if anything goes wrong with the query
     */
    public List<String> findByUser(int userId) throws SQLException {
        String sql = "SELECT skill FROM skills WHERE user_id = ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> skills = new ArrayList<>();
                while (rs.next()) {
                    skills.add(rs.getString("skill"));
                }
                return skills;
            }
        }
    }
}
