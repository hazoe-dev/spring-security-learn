package dev.hazoe.springsecuritydemo.controller;

import dev.hazoe.springsecuritydemo.model.dto.LoginResponse;
import dev.hazoe.springsecuritydemo.model.dto.UserRequest;
import dev.hazoe.springsecuritydemo.model.dto.UserResponse;
import dev.hazoe.springsecuritydemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserRequest user) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(user));
    }
}
