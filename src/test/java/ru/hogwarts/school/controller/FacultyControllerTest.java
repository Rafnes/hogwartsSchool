package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.hogwarts.school.TestData.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerTest {
    @Autowired
    MockMvc mockMvc;

    @SpyBean
    FacultyService facultyService;

    @MockBean
    FacultyRepository facultyRepository;

    @MockBean
    StudentRepository studentRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Корректно получает список всех факультетов")
    void getAllFaculties() throws Exception {
        long id1 = 1L;
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty1.setId(id1);

        long id2 = 2L;
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        faculty2.setId(id2);

        long id3 = 3L;
        Faculty faculty3 = new Faculty(FACULTY_NAME_3, FACULTY_COLOR_3);
        faculty3.setId(id3);

        when(facultyRepository.findAll()).thenReturn(List.of(faculty1, faculty2, faculty3));

        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[0].name").value(faculty1.getName()))
                .andExpect(jsonPath("$[0].color").value(faculty1.getColor()))
                .andExpect(jsonPath("$[1].id").value(id2))
                .andExpect(jsonPath("$[1].name").value(faculty2.getName()))
                .andExpect(jsonPath("$[1].color").value(faculty2.getColor()))
                .andExpect(jsonPath("$[2].id").value(id3))
                .andExpect(jsonPath("$[2].name").value(faculty3.getName()))
                .andExpect(jsonPath("$[2].color").value(faculty3.getColor()));

        verify(facultyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Корректно получает факультет")
    void getFacultyInfo() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);

        when(facultyRepository.findById(id)).thenReturn(Optional.of(faculty));

        mockMvc.perform(get("/faculty/" + faculty.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(faculty.getName()))
                .andExpect(jsonPath("$.color").value(faculty.getColor()));

        verify(facultyRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Корректно получает список студентов факультета")
    void getStudents() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);

        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        student1.setFaculty(faculty);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        student2.setFaculty(faculty);

        when(facultyRepository.findById(id)).thenReturn(Optional.of(faculty));
        when(studentRepository.findAllByFacultyId(id)).thenReturn(List.of(student1, student2));

        mockMvc.perform(get("/faculty/" + id + "/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0].name").value(student1.getName()))
                .andExpect(jsonPath("$[0].age").value(student1.getAge()))
                .andExpect(jsonPath("$[1].name").value(student2.getName()))
                .andExpect(jsonPath("$[1].age").value(student2.getAge()));

        verify(facultyRepository, times(1)).findById(id);
        verify(studentRepository, times(1)).findAllByFacultyId(id);
    }

    @Test
    @DisplayName("Корректно добавляет факультет")
    void createFaculty() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);
        String content = objectMapper.writeValueAsString(faculty);

        when(facultyRepository.save(any(Faculty.class))).thenReturn(faculty);

        mockMvc.perform(post("/faculty")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(faculty.getName()))
                .andExpect(jsonPath("$.color").value(faculty.getColor()));
    }

    @Test
    @DisplayName("Корректно изменяет существующий факультет")
    void editFaculty() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);
        String content = objectMapper.writeValueAsString(faculty);

        when(facultyRepository.findById(id)).thenReturn(Optional.of(faculty));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(faculty);

        mockMvc.perform(put("/faculty")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(faculty.getName()))
                .andExpect(jsonPath("$.color").value(faculty.getColor()));
    }

    @Test
    @DisplayName("Удаляет существующий факультет")
    void deleteFaculty() throws Exception {
        Long id = 1L;

        doNothing().when(facultyRepository).deleteById(id);

        mockMvc.perform(delete("/faculty/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
        verify(facultyRepository).deleteById(id);
    }

    @Test
    @DisplayName("Возвращает факультет по названию")
    void findFacultiesByName() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);

        when(facultyRepository.findByNameIgnoreCase(faculty.getName())).thenReturn(List.of(faculty));

        mockMvc.perform(get("/faculty/find")
                        .param("name", faculty.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].name").value(faculty.getName()))
                .andExpect(jsonPath("$[0].color").value(faculty.getColor()));
        verify(facultyRepository, times(1)).findByNameIgnoreCase(faculty.getName());
    }

    @Test
    @DisplayName("Возвращает факультет по цвету")
    void findFacultiesByColor() throws Exception {
        long id = 1L;
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(id);

        when(facultyRepository.findByColorIgnoreCase(faculty.getColor())).thenReturn(List.of(faculty));

        mockMvc.perform(get("/faculty/find")
                        .param("color", faculty.getColor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].name").value(faculty.getName()))
                .andExpect(jsonPath("$[0].color").value(faculty.getColor()));
        verify(facultyRepository, times(1)).findByColorIgnoreCase(faculty.getColor());
    }
}