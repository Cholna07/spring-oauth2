package kh.edu.istad.platform.oauth2server.mapper;

import kh.edu.istad.platform.oauth2server.dto.UserRequest;
import kh.edu.istad.platform.oauth2server.dto.UserResponse;
import kh.edu.istad.platform.oauth2server.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setEmail(request.email());
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}