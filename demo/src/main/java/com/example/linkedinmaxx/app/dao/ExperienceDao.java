package com.example.linkedinmaxx.app.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//DAO for inserting and querying user experience entries in the database.
public class ExperienceDao {

    /**
     * Saves a single experience entry for the given user.
     *
     * @param userId the user’s database ID
     * @param entry  the experience text to save
     */
    public void save(int userId, String entry) {
        String sql = "INSERT INTO experiences (user_id, entry) VALUES (?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, entry);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving experience entry", e);
        }
    }

    /**
     * Retrieves all experience entries for the given user.
     *
     * @param userId the user’s database ID
     * @return a List of experience strings
     * @throws SQLException if anything goes wrong with the query
     */
    public List<String> findByUser(int userId) throws SQLException {
        String sql = "SELECT entry FROM experiences WHERE user_id = ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> experiences = new ArrayList<>();
                while (rs.next()) {
                    experiences.add(rs.getString("entry"));
                }
                return experiences;
            }
        }
    }
}
