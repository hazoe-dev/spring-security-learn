package dev.hazoe.springsecuritydemo.service;

import dev.hazoe.springsecuritydemo.model.User;
import dev.hazoe.springsecuritydemo.dao.UserRepo;
import dev.hazoe.springsecuritydemo.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepo userRepo;
    @Autowired
    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String username) {
        User foundUser = userRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );
        return new UserPrincipal(foundUser);
    }
}
