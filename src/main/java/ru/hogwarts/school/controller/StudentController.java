package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<Collection<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfo(@PathVariable long id) {
        Student student = studentService.findStudent(id);
        if (student == null) {
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping("{id}/faculty")
    public ResponseEntity<Faculty> getStudentFaculty(@PathVariable long id) {
        Faculty faculty = studentService.findStudent(id).getFaculty();
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student, @RequestParam Long facultyId) {
        return studentService.createStudent(student, facultyId);
    }

    @PutMapping
    public ResponseEntity<Student> updateStudent(@RequestBody Student student, @RequestParam long facultyId) {
        Student s = studentService.updateStudent(student, facultyId);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(s);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Student> deleteStudent(@PathVariable long id) {
        studentService.removeStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("get-by-age")
    public ResponseEntity<Collection<Student>> getStudentsByAge(@RequestParam int age) {
        if (age >= 0) {
            return ResponseEntity.ok(studentService.findByAge(age));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("findByAgeBetween")
    public ResponseEntity<Collection<Student>> findByAgeBetween(@RequestParam int min,
                                                                @RequestParam int max) {
        if (min > 0 && max > 0 && min < max) {
            return ResponseEntity.ok(studentService.findByAgeBetween(min, max));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("amount")
    public Integer getAllStudentsAmount() {
        return studentService.getStudentsAmount();
    }

    @GetMapping("average-age")
    public double getAverageStudentAge() {
        return studentService.getAverageStudentAge();
    }

    @GetMapping("last-five")
    public ResponseEntity<List<Student>> getLastFiveStudents() {
        return ResponseEntity.ok(studentService.getLastFiveStudents());
    }
}
