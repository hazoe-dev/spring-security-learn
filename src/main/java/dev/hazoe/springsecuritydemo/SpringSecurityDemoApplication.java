package dev.hazoe.springsecuritydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class SpringSecurityDemoApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SpringSecurityDemoApplication.class, args);

        //Default filter chain from spring security
        SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
        DefaultSecurityFilterChain defaultFilterChain = context.getBean(DefaultSecurityFilterChain.class);

        System.out.println("Filter chain executed" + filterChain.getFilters());
        System.out.println("Default filter chain executed" + defaultFilterChain.getFilters());
    }

}
