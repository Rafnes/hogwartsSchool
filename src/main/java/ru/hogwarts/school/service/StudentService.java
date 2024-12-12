package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Autowired
    FacultyRepository facultyRepository;

    Logger logger = LoggerFactory.getLogger(StudentService.class);

    public Student createStudent(Student student, Long facultyId) {
        logger.info("Creating new student");
        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow(() -> {
            logger.error("Faculty with id: {} not found", facultyId);
            return new NoSuchElementException();
        });
        student.setFaculty(faculty);
        logger.debug("Faculty with id: {} set to created student. Saving student", facultyId);
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        logger.info("Method findStudent was invoked");
        return studentRepository.findById(id).get();
    }

    public Student updateStudent(Student student, Long facultyId) {
        logger.info("Method updateStudent was invoked");
        Faculty faculty = facultyRepository.findById(facultyId).orElseThrow(() -> {
            logger.error("Student with id: {} not found", facultyId);
            return new NoSuchElementException();
        });
        student.setFaculty(faculty);
        return studentRepository.save(student);
    }

    public void removeStudent(long id) {
        logger.info("Removing student with id: {}", id);
        if (!studentRepository.existsById(id)) {
            logger.warn("Student with id: {} not found, there is no entity to delete", id);
        }
        studentRepository.deleteById(id);
    }

    public Collection<Student> getAll() {
        logger.info("Getting all students");
        return studentRepository.findAll();
    }

    public Collection<Student> findByAge(int age) {
        logger.info("Method findByAge was invoked");
        return studentRepository.findByAge(age);
    }

    public List<Student> findByAgeBetween(int min, int max) {
        logger.info("Method findByAgeBetween was invoked");
        return studentRepository.findByAgeBetween(min, max);
    }

    public int getStudentsAmount() {
        logger.info("Method getStudentsAmount was invoked");
        return studentRepository.getStudentsAmount();
    }

    public double getAverageStudentAge() {
        logger.info("Method getAverageStudentAge was invoked");
        return studentRepository.getAverageStudentAge();
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Method getLastFiveStudents was invoked");
        return studentRepository.getLastFiveStudents();
    }

    public List<String> getNamesThatStartWithAInAlphaOrder() {
        logger.info("Method getNamesThatStartWithAInAlphaOrder was invoked");
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .filter(s -> s.getName().startsWith("A"))
                .map(s -> s.getName().toUpperCase())
                .sorted()
                .toList();
    }

    public double getAverageStudentAgeStream() {
        logger.info("Method getAverageStudentAgeStream was invoked");
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .mapToDouble(Student::getAge)
                .average()
                .orElse(0.0);
    }

    public int sumMethodStream() {
        long start = System.currentTimeMillis();
        int sum = Stream.iterate(1, a -> a + 1)
                .limit(1_000_000)
                .reduce(0, (a, b) -> a + b);
        logger.info("Time elapsed in sequential: {}", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        int sum2 = IntStream.rangeClosed(1, 1_000_000)
                .parallel()
                .reduce(0, (a, b) -> a + b);
        logger.info("Time elapsed in parallel: {}", System.currentTimeMillis() - start);
        return sum2;
    }

    public String printParallel() {
        List<Student> students = studentRepository.findAll().stream()
                .limit(6)
                .toList();
        System.out.println(students.get(0).getName());
        System.out.println(students.get(1).getName());

        Thread thread1 = new Thread(() -> {
            System.out.println(students.get(2).getName());
            System.out.println(students.get(3).getName());
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            System.out.println(students.get(4).getName());
            System.out.println(students.get(5).getName());
        });
        thread2.start();

        return "Printing students in parallel mode";
    }

    public String printSynchronized() {
        List<Student> students = studentRepository.findAll().stream()
                .limit(6)
                .toList();
        System.out.println(students.get(0).getName());
        System.out.println(students.get(1).getName());

        Thread thread1 = new Thread(() -> {
            printStudentNamesSynchronized(students.get(2), students.get(3));
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            printStudentNamesSynchronized(students.get(4), students.get(5));
        });
        thread2.start();
        return "Printing students in synchronized mode";
    }

    private void printStudentNamesSynchronized(Student student1, Student student2) {
        synchronized (this) {
            System.out.println(student1.getName());
            System.out.println(student2.getName());
        }
    }
}