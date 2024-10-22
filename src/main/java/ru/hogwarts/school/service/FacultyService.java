package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;

import java.util.*;

@Service
public class FacultyService {
    private final Map<Long, Faculty> faculties = new HashMap<>();
    private long idCounter;

    public Faculty createFaculty(Faculty faculty) {
        faculty.setId(idCounter++);
        faculties.put(idCounter, faculty);
        return faculty;
    }

    public Faculty findFaculty(long id) {
        return faculties.get(id);
    }

    public Faculty updateFaculty(Faculty faculty) {
        if (!faculties.containsKey(faculty.getId())) {
            throw new IllegalArgumentException();
        } else {
            faculties.put(faculty.getId(), faculty);
            return faculty;
        }
    }

    public Faculty removeFaculty(long id) {
        return faculties.remove(id);
    }

    public Collection<Faculty> getAll() {
        return new ArrayList<>(faculties.values());
    }

    public Collection<Faculty> getFacultiesByColor(String color) {
        List<Faculty> resultList = new ArrayList<>();
        for (Faculty f : faculties.values()) {
            if (f.getColor().equals(color)) {
                resultList.add(f);
            }
        }
        return resultList;
    }
}
