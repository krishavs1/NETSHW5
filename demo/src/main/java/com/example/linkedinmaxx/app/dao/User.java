package com.example.linkedinmaxx.app.dao;


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

   
    public int getId() {
        return id;
    }

    
    public String getEmail() {
        return email;
    }

 
    public String getPasswordHash() {
        return passwordHash;
    }


    public String getUsername() {
        return username;
    }


    public String getSchool() {
        return school;
    }


    public String getMajor() {
        return major;
    }


    public Integer getGradYear() {
        return gradYear;
    }


    public String getInterests() {
        return interests;
    }


    public String getBio() {
        return bio;
    }


    public boolean isRegistered() {
        return registered;
    }
}
