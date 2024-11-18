package ru.hogwarts.school.controller.testRestTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.hogwarts.school.TestData.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    FacultyRepository facultyRepository;

    @Autowired
    TestRestTemplate restTemplate;

    private String buildUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        Faculty faculty3 = new Faculty(FACULTY_NAME_3, FACULTY_COLOR_3);
        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);
        facultyRepository.save(faculty3);
    }

    @Test
    void testGetAllStudents() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty1 = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty1);

        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Faculty faculty2 = facultyRepository.findById(2L).orElseThrow();
        student1.setFaculty(faculty2);

        studentRepository.save(student1);
        studentRepository.save(student2);

        ParameterizedTypeReference<Collection<Student>> responseType = new ParameterizedTypeReference<>() {
        };

        //test
        ResponseEntity<Collection<Student>> response = restTemplate.exchange(
                buildUrl("/student"),
                HttpMethod.GET,
                null,
                responseType);

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertTrue(response.getBody().contains(student1));
        assertTrue(response.getBody().contains(student2));
    }


    @Test
    void createStudentAddsStudentToDb() {
        Student student = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = facultyRepository.findById(1L).orElseThrow();
        student.setFaculty(faculty);

        //test
        ResponseEntity<Student> response = restTemplate.postForEntity
                (buildUrl("/student?facultyId=") + faculty.getId(),
                        student,
                        Student.class);

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Student body = response.getBody();

        assertTrue(studentRepository.existsById(body.getId()));
        Student savedStudent = studentRepository.findById(body.getId()).get();
        assertEquals(savedStudent.getName(), student.getName());
        assertEquals(savedStudent.getAge(), student.getAge());
        assertEquals(savedStudent.getFaculty(), student.getFaculty());
    }

    @Test
    void testGetStudentInfo() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty);

        studentRepository.save(student1);

        //test
        ResponseEntity<Student> response = restTemplate.getForEntity(
                buildUrl("/student/{id}"),
                Student.class,
                student1.getId()
        );

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Student body = response.getBody();
        assertEquals(student1.getName(), body.getName());
        assertEquals(student1.getAge(), body.getAge());
        assertEquals(student1.getFaculty(), studentRepository.findById(student1.getId()).get().getFaculty());
    }

    @Test
    void testRemoveStudentRemovesStudentFromDb() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty);

        studentRepository.save(student1);
        assertTrue(studentRepository.existsById(student1.getId()));

        //test
        ResponseEntity<Student> response = restTemplate.exchange
                (buildUrl("/student/{id}"),
                        HttpMethod.DELETE,
                        null,
                        Student.class,
                        student1.getId());

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        assertFalse(studentRepository.existsById(student1.getId()));
    }

    @Test
    void testUpdateStudent() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty);
        studentRepository.save(student1);

        Student updatedStudent = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        updatedStudent.setId(student1.getId());
        Faculty faculty2 = facultyRepository.findById(2L).orElseThrow();
        updatedStudent.setFaculty(faculty2);

        //test
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> requestEntity = new HttpEntity<>(updatedStudent, headers);
        ResponseEntity<Student> response = restTemplate.exchange(
                buildUrl("/student?facultyId=" + faculty2.getId()),
                HttpMethod.PUT,
                requestEntity,
                Student.class);
        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Student body = response.getBody();
        assertTrue(studentRepository.existsById(body.getId()));
        assertEquals(updatedStudent.getName(), body.getName());
        assertEquals(updatedStudent.getAge(), body.getAge());
        assertEquals(updatedStudent.getFaculty(), studentRepository.findById(body.getId()).get().getFaculty());

        Student resultStudent = studentRepository.findById(body.getId()).get();
        assertEquals(updatedStudent.getName(), resultStudent.getName());
        assertEquals(updatedStudent.getAge(), resultStudent.getAge());
        assertEquals(updatedStudent.getFaculty(), resultStudent.getFaculty());
    }

    @Test
    void testGetByAge() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty1 = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty1);

        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Faculty faculty2 = facultyRepository.findById(2L).orElseThrow();
        student1.setFaculty(faculty2);

        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        Faculty faculty3 = facultyRepository.findById(3L).orElseThrow();
        student1.setFaculty(faculty3);

        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);

        ParameterizedTypeReference<Collection<Student>> responseType = new ParameterizedTypeReference<>() {
        };

        //test
        ResponseEntity<Collection<Student>> response = restTemplate.exchange(
                buildUrl("/student/get-by-age?age=" + STUDENT_AGE_1),
                HttpMethod.GET,
                null,
                responseType);

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertTrue(response.getBody().contains(student1));

        assertFalse(response.getBody().contains(student2));
        assertFalse(response.getBody().contains(student3));
    }

    @Test
    void testFindByAgeBetween() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty1 = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty1);

        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        Faculty faculty2 = facultyRepository.findById(2L).orElseThrow();
        student1.setFaculty(faculty2);

        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        Faculty faculty3 = facultyRepository.findById(3L).orElseThrow();
        student1.setFaculty(faculty3);

        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);

        ParameterizedTypeReference<Collection<Student>> responseType = new ParameterizedTypeReference<>() {
        };

        //test
        ResponseEntity<Collection<Student>> response = restTemplate.exchange(
                buildUrl("/student/findByAgeBetween?min=" + 10 + "&max=" + STUDENT_AGE_2),
                HttpMethod.GET,
                null,
                responseType);

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        assertTrue(response.getBody().contains(student1));
        assertTrue(response.getBody().contains(student2));

        assertFalse(response.getBody().contains(student3));
    }

    @Test
    void testGetStudentFaculty() {
        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        Faculty faculty1 = facultyRepository.findById(1L).orElseThrow();
        student1.setFaculty(faculty1);

        studentRepository.save(student1);

        //test
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                buildUrl("/student/{id}/faculty"),
                Faculty.class,
                student1.getId());

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Faculty body = response.getBody();
        assertEquals(faculty1, body);
    }
}