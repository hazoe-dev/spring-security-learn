package dev.hazoe.springsecuritydemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Welcome";
    }

    @GetMapping("hello")
    public String sayHello(HttpServletRequest request) {
        return "Hello World" + request.getSession().getId();
    }

    @GetMapping("greet")
    public String greet(HttpServletRequest request) {
        return "How are you?" + request.getSession().getId();
    }
}
