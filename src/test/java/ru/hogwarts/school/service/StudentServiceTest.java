package ru.hogwarts.school.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {
    Student student1 = new Student("Lewis Hamilton", 15);
    Student student2 = new Student("Sebastian Vettel", 14);

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testCreateStudent() {
        when(studentRepository.save(student1)).thenReturn(student1);
        Student actual = studentService.createStudent(student1, 1L);
        assertEquals(student1.getName(), actual.getName());
        verify(studentRepository).save(student1);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudent(new Student("", 10), 1L));
    }

    @Test
    void shouldThrowWhenAgeIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudent(new Student("John", 0), 1L));
    }

    @Test
    void testFindStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        Student actual = studentService.findStudent(1L);
        assertEquals(student1.getName(), actual.getName());
        verify(studentRepository).findById(1L);
    }

    @Test
    void testFindStudentNegative() {
        assertThrows(NoSuchElementException.class, () -> studentService.findStudent(1000));
    }

    @Test
    void testUpdateStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        Student actual = studentService.findStudent(1L);
        assertEquals(student1.getName(), actual.getName());
        assertEquals(actual, student1);
        verify(studentRepository).findById(1L);
    }

    @Test
    void testUpdateStudentNegative() {
        assertThrows(NoSuchElementException.class, () -> studentService.findStudent(1000));
    }

    @Test
    void testDeleteStudent() {
        doNothing().when(studentRepository).deleteById(1L);
        studentService.removeStudent(1L);
        verify(studentRepository).deleteById(1L);
    }

    @Test
    void testGetAll() {
        List<Student> students = Arrays.asList(student1, student2);
        when(studentRepository.findAll()).thenReturn(students);

        Collection<Student> result = studentService.getAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(student1));
        assertTrue(result.contains(student2));
        verify(studentRepository).findAll();
    }

    @Test
    void testFindByAge() {
        when(studentRepository.findByAge(15)).thenReturn(List.of(student1));

        List<Student> result = (List<Student>) studentService.findByAge(15);
        assertTrue(result.contains(student1));
        verify(studentRepository).findByAge(15);
    }
}