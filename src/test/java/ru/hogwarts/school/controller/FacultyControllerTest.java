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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.service.FacultyService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.hogwarts.school.TestData.FACULTY_COLOR_1;
import static ru.hogwarts.school.TestData.FACULTY_NAME_1;

@WebMvcTest(FacultyController.class)
class FacultyControllerTest {
    @Autowired
    MockMvc mockMvc;

    @SpyBean
    FacultyService facultyService;

    @MockBean
    FacultyRepository facultyRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Корректно получает список всех факультетов")
    void getAllFaculties() {
    }

    @Test
    void getFacultyInfo() {
    }

    @Test
    void getStudents() {
    }

    @Test
    void createFaculty() throws Exception {
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        String content = objectMapper.writeValueAsString(faculty);
        System.out.println("content = " + content);

        mockMvc.perform(post("/faculty")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void editFaculty() {
    }

    @Test
    void deleteFaculty() {
    }

    @Test
    void findFaculties() {
    }
}