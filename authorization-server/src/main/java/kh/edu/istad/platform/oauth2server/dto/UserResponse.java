package kh.edu.istad.platform.oauth2server.dto;


public record UserResponse(
        Long id,
        String username,
        String email
) {
}

