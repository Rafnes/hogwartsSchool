package ru.hogwarts.school.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Аватар не найден")
public class AvatarNotFoundException extends RuntimeException{
    public AvatarNotFoundException(String message) {
        super(message);
    }

    public AvatarNotFoundException() {
        super();
    }
}
