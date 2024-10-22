package ru.hogwarts.school.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    public String hello() {
        return "Hello there!";
    }
}
