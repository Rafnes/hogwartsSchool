package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exceptions.AvatarNotFoundException;
import ru.hogwarts.school.exceptions.ImageReadFailureException;
import ru.hogwarts.school.exceptions.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.model.dto.AvatarView;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class AvatarService {
    @Value("${image.path}")
    private Path pathDir;
    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    public long uploadAvatar(long studentId, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException("Студент с id" + studentId + " не найден"));
        Path path = saveAvatarLocal(file);

        Avatar avatar = new Avatar(path.toString(), file.getSize(), file.getContentType(), file.getBytes(), student);

        Avatar oldAvatar = avatarRepository.findByStudentId(studentId);
        if (oldAvatar != null) {
            Files.delete(Path.of(oldAvatar.getFilePath()));
            avatar.setId(oldAvatar.getId());
        }
        return avatarRepository.save(avatar).getId();
    }

    private String getExtension(String string) {
        return string.substring(string.lastIndexOf("."));
    }

    private Path saveAvatarLocal(MultipartFile file) throws IOException {
        createDirectoryIfNotExists();

        if (file.getOriginalFilename() == null) {
            throw new RuntimeException("Некорректное изображение");
        }
        Path path = Path.of(pathDir.toString(), UUID.randomUUID() + getExtension(file.getOriginalFilename()));

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file.getBytes())); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            bis.transferTo(bos);
        }

        return path;
    }

    private void createDirectoryIfNotExists() {
        try {
            if (Files.notExists(Path.of(pathDir.toString()))) {
                Files.createDirectory(Path.of(pathDir.toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для аватаров");
        }
    }

    public Avatar getAvatarFromDb(long studentId) {
        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            throw new AvatarNotFoundException("Аватар не найден");
        }
        return avatar;
    }

    public AvatarView getAvatarFromDirectory(long studentId) {
        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            throw new AvatarNotFoundException("Аватар не найден");
        }
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Path.of(avatar.getFilePath()));
        } catch (IOException e) {
            String errorMessage = String.format("Не удалось прочитать файл с изображением по пути: %s. %s", avatar.getFilePath(), e.getMessage());
            throw new ImageReadFailureException(errorMessage, e);
        }
        return new AvatarView(MediaType.parseMediaType(avatar.getMediaType()), bytes);
    }

    public void setPathDir(Path pathDir) {
        this.pathDir = pathDir;
    }

    public List<Avatar> getAvatars(Integer pageNumber, Integer pageSize) {
        if (pageSize > 0 && pageNumber > 0) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
            return avatarRepository.findAll(pageRequest).getContent();
        } else {
            throw new IllegalArgumentException("Переданы некорректные параметры страницы");
        }
    }
}
