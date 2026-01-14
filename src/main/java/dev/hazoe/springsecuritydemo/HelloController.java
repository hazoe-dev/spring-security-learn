package dev.hazoe.springsecuritydemo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("hello")
    public String sayHello(HttpServletRequest request) {
        return "Hello World" + request.getSession().getId();
    }

    //Session to keep login information (credential) for any time, any resources later
    @GetMapping("greet")
    public String greet(HttpServletRequest request) {
        return "How are you?" + request.getSession().getId();
    }
}
