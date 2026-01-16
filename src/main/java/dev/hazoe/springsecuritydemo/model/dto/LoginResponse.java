package dev.hazoe.springsecuritydemo.model.dto;

public record LoginResponse(
        String username,
        String accessToken
) {}

