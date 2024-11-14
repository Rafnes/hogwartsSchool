package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collection;

@RestController
@RequestMapping("/faculty")
public class FacultyController {
    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<Collection<Faculty>> getAllFaculties() {
        return ResponseEntity.ok(facultyService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<Faculty> getFacultyInfo(@PathVariable long id) {
        Faculty faculty = facultyService.findFaculty(id);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(faculty);
        }
    }

    @GetMapping("{id}/students")
    public ResponseEntity<Collection<Student>> getStudents(@PathVariable long id) {
        return ResponseEntity.ok(facultyService.getStudentsByFaculty(id));
    }

    @PostMapping
    public Faculty createFaculty(@RequestBody Faculty faculty) {
        return facultyService.createFaculty(faculty);
    }

    @PutMapping
    public ResponseEntity<Faculty> editFaculty(@RequestBody Faculty faculty) {
        Faculty f = facultyService.editFaculty(faculty);
        if (f == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(f);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Faculty> deleteFaculty(@PathVariable long id) {
        facultyService.removeFaculty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find")
    public ResponseEntity<Collection<Faculty>> findFaculties(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String color) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(facultyService.findFacultiesByName(name));
        } else if (color != null && !color.isBlank()) {
            return ResponseEntity.ok(facultyService.findFacultiesByColor(color));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
