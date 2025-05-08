package com.example.linkedinmaxx.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DB {
    // URL for the LinkedInMaxx database
    private static final String URL = Optional
        .ofNullable(System.getenv("DB_URL"))
        .orElse("jdbc:postgresql://localhost:5432/linkedinmaxx");

    // Database user
    private static final String USER = Optional
        .ofNullable(System.getenv("DB_USER"))
        .orElse("krishavsingla");

    // Database password
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

    //Get a new connection to the database

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    
    public static Connection getConnection() throws SQLException {
        return get();
    }
}
