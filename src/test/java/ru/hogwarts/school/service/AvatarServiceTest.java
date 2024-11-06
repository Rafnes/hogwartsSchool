package ru.hogwarts.school.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {
    @Mock
    AvatarRepository avatarRepository;

    @Mock
    StudentRepository studentRepository;

    @InjectMocks
    AvatarService avatarService;

    @Mock
    MultipartFile mockFile;

    Student student = new Student("Олег Тестов", 11);

    @BeforeEach
    void setUp() throws IOException {
        Path testDir = Files.createTempDirectory("test-avatars");
        avatarService.setPathDir(testDir);
    }


    @Test
    void uploadAvatarToExistingStudent() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("test", "test.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
        Avatar avatar = new Avatar();
        avatar.setId(1L);

        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));
        when(avatarRepository.save(any(Avatar.class))).thenReturn(avatar);

        //test
        long actualId = avatarService.uploadAvatar(1L, mockFile);

        //check
        assertEquals(actualId, avatar.getId());
    }
}