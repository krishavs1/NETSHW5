package com.example.linkedinmaxx.app.dao;

/**
 * Simple POJO that represents one row in the users table.
 */
public class User {
    private final int    id;
    private final String email;
    private final String passwordHash;
    private final String username;
    private final String school;
    private final String major;
    private final Integer gradYear;
    private final String interests;
    private final String bio;
    private final boolean registered;

    public User(int id,
                String email,
                String passwordHash,
                String username,
                String school,
                String major,
                Integer gradYear,
                String interests,
                String bio,
                boolean registered) {
        this.id           = id;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.username     = username;
        this.school       = school;
        this.major        = major;
        this.gradYear     = gradYear;
        this.interests    = interests;
        this.bio          = bio;
        this.registered   = registered;
    }

    /** The database‑generated primary key. */
    public int getId() {
        return id;
    }

    /** User’s email address (unique). */
    public String getEmail() {
        return email;
    }

    /** The bcrypt hash of the user’s password. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** User’s chosen (unique) handle. */
    public String getUsername() {
        return username;
    }

    /** School name (nullable). */
    public String getSchool() {
        return school;
    }

    /** Major field of study (nullable). */
    public String getMajor() {
        return major;
    }

    /** Graduation year (nullable). */
    public Integer getGradYear() {
        return gradYear;
    }

    /** Comma‑separated interests string (nullable). */
    public String getInterests() {
        return interests;
    }

    /** Bio or about‑me text (nullable). */
    public String getBio() {
        return bio;
    }

    /** Indicates whether the user completed registration. */
    public boolean isRegistered() {
        return registered;
    }
}
