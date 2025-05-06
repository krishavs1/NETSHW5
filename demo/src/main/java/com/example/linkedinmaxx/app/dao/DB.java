package com.example.linkedinmaxx.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DB {
    // JDBC URL for the LinkedInMaxx database with a sensible default
    private static final String URL = Optional
        .ofNullable(System.getenv("DB_URL"))
        .orElse("jdbc:postgresql://localhost:5432/linkedinmaxx");

    // Database user: default to your OS/DB user
    private static final String USER = Optional
        .ofNullable(System.getenv("DB_USER"))
        .orElse("krishavsingla");

    // Database password: default to "password" for local dev
    private static final String PASS = Optional
        .ofNullable(System.getenv("DB_PASS"))
        .orElse("password");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Get a new connection to the database, using URL, USER, PASS.
     */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Alias for get(); some DAOs expect getConnection().
     */
    public static Connection getConnection() throws SQLException {
        return get();
    }
}
