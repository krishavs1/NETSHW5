package com.example.linkedinmaxx.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the users table.
 */
public class UserDao {

  //quickly test whether any user has this email
  public boolean existsByEmail(String email) throws SQLException {
    String sql = "SELECT 1 FROM users WHERE email = ?";
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

 
  public boolean existsByUsername(String username) throws SQLException {
    String sql = "SELECT 1 FROM users WHERE username = ?";
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  // Look up a user by their username.
  public Optional<User> findByUsername(String username) throws SQLException {
    String sql = """
      SELECT id,email,password,username,school,major,grad_year,interests,bio,registered
        FROM users
       WHERE username = ?
      """;
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapRowToUser(rs));
      }
    }
  }

  //Fetch every user in the table
  public List<User> findAll() throws SQLException {
    String sql = """
      SELECT id,email,password,username,school,major,grad_year,interests,bio,registered
        FROM users
      """;
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      List<User> users = new ArrayList<>();
      while (rs.next()) {
        users.add(mapRowToUser(rs));
      }
      return users;
    }
  }

  //Create a new user row and return its generated ID.
  public int create(String email,
                    String hashedPw,
                    String username,
                    String school,
                    String major,
                    Integer gradYear,
                    String interests,
                    String bio) throws SQLException {
    String sql = """
      INSERT INTO users(
        email,password,username,school,major,grad_year,interests,bio
      ) VALUES(?,?,?,?,?,?,?,?)
      RETURNING id
      """;
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setString(1, email);
      ps.setString(2, hashedPw);
      ps.setString(3, username);
      ps.setString(4, school);
      ps.setString(5, major);
      if (gradYear != null) {
        ps.setInt(6, gradYear);
      } else {
        ps.setNull(6, Types.INTEGER);
      }
      ps.setString(7, interests);
      ps.setString(8, bio);

      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  //Look up one user by their numeric id

  public Optional<User> findById(int id) throws SQLException {
    String sql = """
      SELECT id,email,password,username,school,major,grad_year,interests,bio,registered
        FROM users
       WHERE id = ?
      """;
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        return Optional.of(mapRowToUser(rs));
      }
    }
  }

 
  private User mapRowToUser(ResultSet rs) throws SQLException {
    return new User(
      rs.getInt("id"),
      rs.getString("email"),
      rs.getString("password"),
      rs.getString("username"),
      rs.getString("school"),
      rs.getString("major"),
      rs.getObject("grad_year", Integer.class),
      rs.getString("interests"),
      rs.getString("bio"),
      rs.getBoolean("registered")
    );
  }

 
  public Optional<User> findByEmail(String email) throws SQLException {
    String sql = """
      SELECT id,email,password,username,school,major,grad_year,interests,bio,registered
        FROM users
       WHERE email = ?
      """;
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        return Optional.of(mapRowToUser(rs));
      }
    }
  }

  
}
