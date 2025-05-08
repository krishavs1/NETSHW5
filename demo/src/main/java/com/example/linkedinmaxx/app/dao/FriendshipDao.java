package com.example.linkedinmaxx.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//DAO for creating and querying friendship edges between users.

public class FriendshipDao {

    //Inserts a directed edge into the friendships table.
    public void add(int userId, int friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                String.format("Error saving friendship (%d → %d)", userId, friendId), e
            );
        }
    }

    //Returns every directed friendship edge in the table.
    public List<Friendship> findAll() {
        String sql = "SELECT user_id, friend_id FROM friendships";
        List<Friendship> edges = new ArrayList<>();
        try (Connection conn = DB.get();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                edges.add(new Friendship(
                    rs.getInt("user_id"),
                    rs.getInt("friend_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all friendships", e);
        }
        return edges;
    }

    //Returns the list of friend ids for a given user
    
    public List<Integer> findFriends(int userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Integer> friends = new ArrayList<>();
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friends.add(rs.getInt("friend_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                String.format("Error fetching friends of user %d", userId), e
            );
        }
        return friends;
    }
}
