package ru.hogwarts.school.controller.testRestTemplate;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.hogwarts.school.TestData.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    FacultyRepository facultyRepository;

    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TestRestTemplate restTemplate;

    private String buildUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private <R> void assertResponseOkNotNullHasBody(ResponseEntity<R> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void getAllFaculties() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        Faculty faculty3 = new Faculty(FACULTY_NAME_3, FACULTY_COLOR_3);

        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);
        facultyRepository.save(faculty3);

        ParameterizedTypeReference<Collection<Faculty>> responseType = new ParameterizedTypeReference<>() {
        };

        //test
        ResponseEntity<Collection<Faculty>> response = restTemplate.exchange(
                buildUrl("/faculty"),
                HttpMethod.GET,
                null,
                responseType);

        //check
        assertResponseOkNotNullHasBody(response);
        Collection<Faculty> body = response.getBody();
        assertFalse(body.isEmpty());
        assertEquals(3, body.size());
    }

    @Test
    void getFacultyInfo() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);

        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);

        //test
        ResponseEntity<Faculty> responseFaculty1 = restTemplate.getForEntity(
                buildUrl("/faculty/{id}"),
                Faculty.class,
                faculty1.getId()
        );
        System.out.println("responseFaculty1 = " + responseFaculty1);

        ResponseEntity<Faculty> responseFaculty2 = restTemplate.getForEntity(
                buildUrl("/faculty/{id}"),
                Faculty.class,
                faculty2.getId()
        );
        System.out.println("responseFaculty2 = " + responseFaculty2);

        //check
        assertResponseOkNotNullHasBody(responseFaculty1);
        assertResponseOkNotNullHasBody(responseFaculty2);

        Faculty body1 = responseFaculty1.getBody();
        Faculty body2 = responseFaculty2.getBody();

        assertEquals(faculty1, body1);
        assertEquals(faculty2, body2);
    }

    @Test
    void getStudentsReturnsValidCollection() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);

        Student student1 = new Student(STUDENT_NAME_1, STUDENT_AGE_1);
        student1.setFaculty(faculty1);
        Student student2 = new Student(STUDENT_NAME_2, STUDENT_AGE_2);
        student2.setFaculty(faculty1);
        Student student3 = new Student(STUDENT_NAME_3, STUDENT_AGE_3);
        student3.setFaculty(faculty2);
        Student student4 = new Student(STUDENT_NAME_4, STUDENT_AGE_4);
        student4.setFaculty(faculty2);

        studentRepository.save(student1);
        studentRepository.save(student2);
        studentRepository.save(student3);
        studentRepository.save(student4);

        ParameterizedTypeReference<Collection<Student>> responseType = new ParameterizedTypeReference<>() {
        };

        //test
        ResponseEntity<Collection<Student>> responseFaculty1 = restTemplate.exchange(
                buildUrl("/faculty/{id}/students"),
                HttpMethod.GET,
                null,
                responseType,
                faculty1.getId()
        );
        ResponseEntity<Collection<Student>> responseFaculty2 = restTemplate.exchange(
                buildUrl("/faculty/{id}/students"),
                HttpMethod.GET,
                null,
                responseType,
                faculty2.getId()
        );

        assertResponseOkNotNullHasBody(responseFaculty1);
        assertResponseOkNotNullHasBody(responseFaculty2);

        Collection<Student> body1 = responseFaculty1.getBody();
        Collection<Student> body2 = responseFaculty2.getBody();

        assertThat(body1).containsAll(List.of(student1, student2));
        assertThat(body2).containsAll(List.of(student3, student4));
    }

    @Test
    void createFacultyAddsFacultyToDb() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);

        //test
        ResponseEntity<Faculty> response = restTemplate.postForEntity
                (buildUrl("/faculty"),
                        faculty1,
                        Faculty.class);

        //check
        assertResponseOkNotNullHasBody(response);
        Faculty body = response.getBody();
        assertEquals(faculty1.getName(), body.getName());
        assertEquals(faculty1.getColor(), body.getColor());
        assertTrue(facultyRepository.existsById(body.getId()));
    }

    @Test
    void editFacultyUpdatedFacultyInDb() {
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        facultyRepository.save(faculty);

        Faculty updatedFaculty = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        updatedFaculty.setId(faculty.getId());

        //test
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(updatedFaculty, headers);
        ResponseEntity<Faculty> response = restTemplate.exchange(
                buildUrl("/faculty"),
                HttpMethod.PUT,
                requestEntity,
                Faculty.class
        );

        //check
        assertResponseOkNotNullHasBody(response);
        Faculty body = response.getBody();
        assertTrue(facultyRepository.existsById(body.getId()));
        List<Faculty> list = facultyRepository.findByNameIgnoreCase(faculty.getName());
        assertTrue(list.isEmpty());
        assertEquals(updatedFaculty.getName(), body.getName());
        assertEquals(updatedFaculty.getColor(), body.getColor());

        Faculty savedFaculty = facultyRepository.findById(body.getId()).get();
        assertEquals(updatedFaculty.getName(), savedFaculty.getName());
        assertEquals(updatedFaculty.getColor(), savedFaculty.getColor());
    }

    @Test
    void deleteFacultyRemovesFacultyFromDb() {
        Faculty faculty = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        facultyRepository.save(faculty);

        assertTrue(facultyRepository.existsById(faculty.getId()));

        //test
        ResponseEntity<Faculty> response = restTemplate.exchange(
                buildUrl("/faculty/{id}"),
                HttpMethod.DELETE,
                null,
                Faculty.class,
                faculty.getId()
        );
        System.out.println("response = " + response);

        //check
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        assertFalse(facultyRepository.existsById(faculty.getId()));
    }

    @Test
    void findFacultiesByNameReturnsValidCollection() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);

        ParameterizedTypeReference<Collection<Faculty>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Collection<Faculty>> responseByName = restTemplate.exchange(
                buildUrl("/faculty/find?name=" + faculty1.getName()),
                HttpMethod.GET,
                null,
                responseType
        );

        //check
        assertResponseOkNotNullHasBody(responseByName);
        Collection<Faculty> found = responseByName.getBody();
        assertTrue(found.contains(faculty1));
        assertFalse(found.contains(faculty2));
    }

    @Test
    void findFacultiesByColorReturnsValidCollection() {
        Faculty faculty1 = new Faculty(FACULTY_NAME_1, FACULTY_COLOR_1);
        Faculty faculty2 = new Faculty(FACULTY_NAME_2, FACULTY_COLOR_2);
        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);

        ParameterizedTypeReference<Collection<Faculty>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Collection<Faculty>> responseByName = restTemplate.exchange(
                buildUrl("/faculty/find?color=" + faculty1.getColor()),
                HttpMethod.GET,
                null,
                responseType
        );

        //check
        assertResponseOkNotNullHasBody(responseByName);
        Collection<Faculty> found = responseByName.getBody();
        assertTrue(found.contains(faculty1));
        assertFalse(found.contains(faculty2));
    }
}