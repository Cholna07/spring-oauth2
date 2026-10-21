package kh.edu.istad.platform.oauth2server.dto;

public record UserRequest(
        String username,
        String password,
        String email
) {
}
