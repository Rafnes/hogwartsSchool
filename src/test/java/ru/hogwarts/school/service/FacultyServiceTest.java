package ru.hogwarts.school.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class FacultyServiceTest {
    Faculty faculty1 = new Faculty("Slytherin", "black");
    Faculty faculty2 = new Faculty("Hufflepuff", "pink");

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private FacultyService facultyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testCreateFaculty() {
        when(facultyRepository.save(faculty1)).thenReturn(faculty1);
        Faculty actual = facultyService.createFaculty(faculty1);
        assertEquals(faculty1.getName(), actual.getName());
        verify(facultyRepository).save(faculty1);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> facultyService.createFaculty(new Faculty("", "white")));
    }

    @Test
    void shouldThrowWhenColorIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> facultyService.createFaculty(new Faculty("Abc", "")));
    }

    @Test
    void testFindFaculty() {
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty1));
        Faculty actual = facultyService.findFaculty(1L);
        assertEquals(faculty1.getName(), actual.getName());
        verify(facultyRepository).findById(1L);
    }

    @Test
    void testFindFacultyNegative() {
        assertThrows(NoSuchElementException.class, () -> facultyService.findFaculty(1000));
    }

    @Test
    void testUpdateFaculty() {
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty1));
        Faculty actual = facultyService.findFaculty(1L);
        assertEquals(faculty1.getName(), actual.getName());
        assertEquals(actual, faculty1);
        verify(facultyRepository).findById(1L);
    }

    @Test
    void testUpdateFacultyNegative() {
        assertThrows(NoSuchElementException.class, () -> facultyService.findFaculty(1000));
    }

    @Test
    void testDeleteFaculty() {
        doNothing().when(facultyRepository).deleteById(1L);
        facultyService.removeFaculty(1L);
        verify(facultyRepository).deleteById(1L);
    }

    @Test
    void testGetAll() {
        List<Faculty> faculties = Arrays.asList(faculty1, faculty2);
        when(facultyRepository.findAll()).thenReturn(faculties);

        Collection<Faculty> result = facultyService.getAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(faculty1));
        assertTrue(result.contains(faculty2));
        verify(facultyRepository).findAll();
    }

    @Test
    void testGetFacultiesByColor() {
        when(facultyRepository.findByColor("black")).thenReturn(List.of(faculty1));

        List<Faculty> result = (List<Faculty>) facultyService.getFacultiesByColor("black");
        assertTrue(result.contains(faculty1));
        verify(facultyRepository).findByColor("black");
    }
}