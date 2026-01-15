package dev.hazoe.springsecuritydemo.service;

import dev.hazoe.springsecuritydemo.dao.UserRepo;
import dev.hazoe.springsecuritydemo.model.Role;
import dev.hazoe.springsecuritydemo.model.User;
import dev.hazoe.springsecuritydemo.model.dto.UserRequest;
import dev.hazoe.springsecuritydemo.model.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepo userRepo;

    @Autowired
    public void setUserRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public UserResponse save(UserRequest user) {
        User newUser = new User();
        newUser.setUsername(user.username());
        newUser.setPassword(user.password());
        newUser.setRole(Role.USER);
        User savedUser = userRepo.save(newUser);
        return new UserResponse(savedUser.getUsername());
    }
}
