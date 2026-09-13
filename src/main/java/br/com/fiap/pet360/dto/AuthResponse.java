package br.com.fiap.pet360.dto;

public record AuthResponse(
        String token,
        String username,
        String role,
        long expiresInMs
) {
}
