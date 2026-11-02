package com.example.r005_student_assignment_tracker;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String userId;
    private String name;
    private String email;
    private long createdAt;

    public UserProfile() {
        this.createdAt = System.currentTimeMillis();
    }

    public UserProfile(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
