package dev.hazoe.springsecuritydemo.dao;

import dev.hazoe.springsecuritydemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
