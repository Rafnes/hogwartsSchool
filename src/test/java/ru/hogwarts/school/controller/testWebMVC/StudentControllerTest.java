package ru.hogwarts.school.controller.testWebMVC;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.hogwarts.school.TestData.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @SpyBean
    StudentService studentService;

    @MockBean
    StudentRepository studentRepository;

    @MockBean
    FacultyRepository facultyRepository;

    @Autowired
    ObjectMapper objectMapper;

    String path = "/student";

    @Test
    @DisplayName("Корректно возвращает список всех студентов")
    void getAllStudents() throws Exception {
        long id1 = 1L, id2 = 2L, id3 = 3L;
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        student1.setId(id1);
        student2.setId(id2);
        student3.setId(id3);

        when(studentRepository.findAll()).thenReturn(List.of(student1, student2, student3));

        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[0].name").value(student1.getName()))
                .andExpect(jsonPath("$[0].age").value(student1.getAge()))
                .andExpect(jsonPath("$[1].id").value(id2))
                .andExpect(jsonPath("$[1].name").value(student2.getName()))
                .andExpect(jsonPath("$[1].age").value(student2.getAge()))
                .andExpect(jsonPath("$[2].id").value(id3))
                .andExpect(jsonPath("$[2].name").value(student3.getName()))
                .andExpect(jsonPath("$[2].age").value(student3.getAge()));

        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Корректно находит студента по id")
    void getStudentInfo() throws Exception {
        long id = 1L;
        Student student = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        student.setId(id);

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        mockMvc.perform(get(path + "/" + student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.age").value(student.getAge()));

        verify(studentRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Корректно возвращает факультет студента")
    void getStudentFaculty() throws Exception {
        long id = 1L;
        Student student = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        student.setId(id);

        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        faculty.setId(1L);
        student.setFaculty(faculty);

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        mockMvc.perform(get(path + "/" + student.getId() + "/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect((jsonPath("$.id")).value(1L))
                .andExpect((jsonPath("$.name")).value(faculty.getName()))
                .andExpect((jsonPath("$.color")).value(faculty.getColor()));

        verify(studentRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Корректно добавляет студента")
    void createStudent() throws Exception {
        long id = 1L;
        Student student = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        student.setId(id);
        faculty.setId(id);
        String content = objectMapper.writeValueAsString(student);

        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(facultyRepository.findById(anyLong())).thenReturn(Optional.of(faculty));

        mockMvc.perform(post(path)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("facultyId", String.valueOf(student.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(jsonPath("$.name").value(student.getName()))
                .andExpect(jsonPath("$.age").value(student.getAge()));
        verify(studentRepository, times(1)).save(student);
        verify(facultyRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Корректно изменяет существующего студента")
    void updateStudent() throws Exception {
        long id = 1L;
        Student student = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        student.setId(id);
        faculty.setId(id);
        String content = objectMapper.writeValueAsString(student);

        long newId = 2L;
        Student newStudent = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Faculty newFaculty = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        newStudent.setId(newId);
        newFaculty.setId(newId);
        String newContent = objectMapper.writeValueAsString(newStudent);

        when(studentRepository.save(newStudent)).thenReturn(newStudent);
        when(facultyRepository.findById(id)).thenReturn(Optional.of(faculty));
        when(facultyRepository.findById(newId)).thenReturn(Optional.of(newFaculty));

        mockMvc.perform(put(path)
                        .content(newContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("facultyId", String.valueOf(newFaculty.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(newStudent.getId()))
                .andExpect(jsonPath("$.name").value(newStudent.getName()))
                .andExpect(jsonPath("$.age").value(newStudent.getAge()));

        verify(studentRepository, times(1)).save(newStudent);
        verify(facultyRepository, times(1)).findById(newId);
    }

    @Test
    @DisplayName("Корректно удаляет студента")
    void deleteStudent() throws Exception {
        Long id = 1L;

        doNothing().when(studentRepository).deleteById(id);

        mockMvc.perform(delete(path + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
        verify(studentRepository).deleteById(id);
    }

    @Test
    @DisplayName("Возвращает список студентов определенного возраста")
    void getStudentsByAge() throws Exception {
        long id1 = 1L, id2 = 2L;
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        student1.setId(id1);
        student2.setId(id2);

        when(studentRepository.findByAge(STUDENT_AGE_1)).thenReturn(List.of(student1));

        mockMvc.perform(get(path + "/get-by-age")
                        .param("age", String.valueOf(STUDENT_AGE_1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$[0].id").value(student1.getId()))
                .andExpect(jsonPath("$[0].name").value(student1.getName()))
                .andExpect(jsonPath("$[0].age").value(student1.getAge()));

        verify(studentRepository, times(1)).findByAge(STUDENT_AGE_1);
    }

    @Test
    @DisplayName("Возвращает студентов с возрастом в промежутке min и max")
    void findByAgeBetween() throws Exception {
        long id1 = 1L, id2 = 2L, id3 = 3L;
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        student1.setId(id1);
        student2.setId(id2);
        student3.setId(id3);

        when(studentRepository.findByAgeBetween(10, STUDENT_AGE_2)).thenReturn(List.of(student1, student2));

        mockMvc.perform(get(path + "/findByAgeBetween")
                        .param("min", String.valueOf(10))
                        .param("max", String.valueOf(STUDENT_AGE_2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(student1.getId()))
                .andExpect(jsonPath("$[0].name").value(student1.getName()))
                .andExpect(jsonPath("$[0].age").value(student1.getAge()))
                .andExpect(jsonPath("$[1].id").value(student2.getId()))
                .andExpect(jsonPath("$[1].name").value(student2.getName()))
                .andExpect(jsonPath("$[1].age").value(student2.getAge()));

        verify(studentRepository, times(1)).findByAgeBetween(10, STUDENT_AGE_2);
    }

    @Test
    @DisplayName("Корректно получает количество студентов")
    void testGetAmount() throws Exception {
        when(studentRepository.getStudentsAmount()).thenReturn(10);

        mockMvc.perform(get(path + "/amount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(10));

        verify(studentRepository, times(1)).getStudentsAmount();
    }

    @Test
    @DisplayName("Корректно получает средний возраст студентов")
    void testGetAverageStudentAge() throws Exception {
        when(studentRepository.getAverageStudentAge()).thenReturn(13.99);

        mockMvc.perform(get(path + "/average-age"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(13.99));

        verify(studentRepository, times(1)).getAverageStudentAge();
    }

    @Test
    @DisplayName("Корректно получает последние пять студентов")
    void testGetLastFiveStudents() {
        long id1 = 1L, id2 = 2L, id3 = 3L, id4 = 4L;
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        Student student4 = new Student(STUDENT_NAME_4, STUDENT_AGE_4);
        student1.setId(id1);
        student2.setId(id2);
        student3.setId(id3);
        student4.setId(id4);

    }
}