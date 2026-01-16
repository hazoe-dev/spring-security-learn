package dev.hazoe.springsecuritydemo.config.init;

import dev.hazoe.springsecuritydemo.model.Role;
import dev.hazoe.springsecuritydemo.model.User;
import dev.hazoe.springsecuritydemo.dao.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class UserDataInitializer implements CommandLineRunner {
    private final UserRepo userRepo;

    @Override
    public void run(String... args) throws Exception {
        if(userRepo.count() == 0) {
            User newUser = new User(null, "ha", "h@123", Role.USER);
            User newAdmin = new User(null, "admin", "a@123", Role.ADMIN);

            userRepo.save(newUser);
            userRepo.save(newAdmin);
        }
    }
}
