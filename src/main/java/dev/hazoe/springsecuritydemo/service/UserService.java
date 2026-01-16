package dev.hazoe.springsecuritydemo.service;

import dev.hazoe.springsecuritydemo.dao.UserRepo;
import dev.hazoe.springsecuritydemo.model.Role;
import dev.hazoe.springsecuritydemo.model.User;
import dev.hazoe.springsecuritydemo.model.dto.LoginResponse;
import dev.hazoe.springsecuritydemo.model.dto.UserRequest;
import dev.hazoe.springsecuritydemo.model.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    @Autowired
    public UserService(UserRepo userRepo,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserResponse save(UserRequest user) {
        User newUser = new User();
        newUser.setUsername(user.username());
        newUser.setPassword(passwordEncoder.encode(user.password()));
        newUser.setRole(Role.USER);
        User savedUser = userRepo.save(newUser);
        return new UserResponse(savedUser.getUsername());
    }

    public LoginResponse login(UserRequest user) {
        //Spring auto handles login fail
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.username(),
                        user.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponse(
                user.username(),
                jwtService.getAccessToken(user.username())
        );
    }
}
