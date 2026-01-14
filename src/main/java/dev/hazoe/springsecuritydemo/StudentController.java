package dev.hazoe.springsecuritydemo;

import dev.hazoe.springsecuritydemo.model.Student;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {

    List<Student> students = List.of(
            new Student(1, "Ha", "Java"),
            new Student(2, "Zoe", "Js"),
            new Student(3, "H", "C")
    );

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
