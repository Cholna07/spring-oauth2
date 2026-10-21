package kh.edu.istad.platform.oauth2server.controller;

import kh.edu.istad.platform.oauth2server.dto.UserRequest;
import kh.edu.istad.platform.oauth2server.dto.UserResponse;
import kh.edu.istad.platform.oauth2server.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }
}