package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.*;

@Service
public class StudentService {
    private final Map<Long, Student> students = new HashMap<>();
    private long idCounter;

    public Student createStudent(Student student) {
        student.setId(++idCounter);
        students.put(idCounter, student);
        return student;
    }

    public Student findStudent(long id) {
        if (students.containsKey(id)) {
            return students.get(id);
        } else {
            return null;
        }
    }

    public Student updateStudent(Student student) {
        if (students.containsKey(student.getId())) {
            students.put(student.getId(), student);
            return student;
        } else {
            return null;
        }
    }

    public Student removeStudent(long id) {
        return students.remove(id);
    }

    public Collection<Student> getAll() {
        return new ArrayList<>(students.values());
    }

    public Collection<Student> findByAge(int age) {
        List<Student> resultList = new ArrayList<>();
        for (Student s : students.values()) {
            if (s.getAge() == age) {
                resultList.add(s);
            }
        }
        return resultList;
    }
}
