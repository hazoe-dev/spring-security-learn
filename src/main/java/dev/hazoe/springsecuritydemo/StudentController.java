package dev.hazoe.springsecuritydemo;

import dev.hazoe.springsecuritydemo.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {

    List<Student> students = new ArrayList<>();

    public StudentController() {
        this.students.add(new Student(1, "Ha", "Java"));
        this.students.add(new Student(2, "Zoe", "Js"));
    }

    @GetMapping( "scrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @GetMapping(path = "students", produces = "application/JSON")
    public List<Student> getAllStudents(){
        return students;
    }

    @GetMapping("students/{id}")
    public Student getAllStudents(@PathVariable int id){
        return students.get(id-1);
    }

    @PostMapping("students")
    public void addStudent(@RequestBody Student student){
        students.add(student);
    }
}
