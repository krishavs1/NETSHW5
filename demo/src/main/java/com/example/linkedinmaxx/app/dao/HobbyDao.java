package com.example.linkedinmaxx.app.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//DAO for inserting user hobbies into the database (we ended up taking this one out)

public class HobbyDao {
    /**
     * Saves a single hobby for a given user
     * @param userId the user’s database ID
     * @param hobby  the hobby text to save
     */
    public void save(int userId, String hobby) {
        String sql = "INSERT INTO hobbies (user_id, hobby) VALUES (?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, hobby);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving hobby", e);
        }
    }
}
