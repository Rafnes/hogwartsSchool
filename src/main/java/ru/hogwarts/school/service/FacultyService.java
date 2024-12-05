package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.*;

@Service
public class FacultyService {
    private final FacultyRepository facultyRepository;

    private final StudentRepository studentRepository;

    Logger logger = LoggerFactory.getLogger(FacultyService.class);

    public FacultyService(FacultyRepository facultyRepository, StudentRepository studentRepository) {
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Creating faculty: {}", faculty.getName());
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(long id) {
        logger.info("Finding faculty with id: {}", id);
        return facultyRepository.findById(id).orElseThrow(() -> {
            logger.error("Faculty with id {} not found", id);
            return new NoSuchElementException("Faculty not found");
        });
    }

    public Faculty editFaculty(Faculty faculty) {
        logger.info("Method editFaculty was invoked");
        return facultyRepository.save(faculty);
    }

    public void removeFaculty(long id) {
        logger.info("Deleting faculty with id: {}", id);
        if(!facultyRepository.existsById(id)) {
            logger.warn("Faculty with id: {} not found, there is no entity to delete", id);
        }
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> getAll() {
        logger.debug("Getting all faculties");
        return facultyRepository.findAll();
    }

    public Collection<Student> getStudentsByFaculty(Long facultyId) {
        logger.info("Method getStudentsByFaculty was invoked");
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> {
                    logger.error("Faculty with id: {} not found", facultyId);
                    return new NoSuchElementException("Факультет не найден");
                });
        return studentRepository.findAllByFacultyId(facultyId);
    }

    public Collection<Faculty> findFacultiesByColor(String color) {
        logger.info("Finding faculties with color: {}", color);
        return facultyRepository.findByColorIgnoreCase(color);
    }

    public Collection<Faculty> findFacultiesByName(String name) {
        logger.info("Finding faculties with name: {}", name);
        return facultyRepository.findByNameIgnoreCase(name);
    }
}
