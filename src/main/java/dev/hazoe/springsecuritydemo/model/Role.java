package dev.hazoe.springsecuritydemo.model;

public enum Role {
    USER, ADMIN;
    public String asRole() {
        return name();
    }
}

