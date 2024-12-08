package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger logger = LoggerFactory.getLogger(AvatarService.class);

    public long uploadAvatar(long studentId, MultipartFile file) throws IOException {
        logger.info("Method uploadAvatar was invoked");
        Student student = studentRepository.findById(studentId).orElseThrow(() -> {
            String errorMessage = "Студент с id " + studentId + " не найден";
            logger.error(errorMessage);
            return new StudentNotFoundException("Студент с id" + studentId + " не найден");
        });
        Path path = saveAvatarLocal(file);

        Avatar avatar = new Avatar(path.toString(), file.getSize(), file.getContentType(), file.getBytes(), student);

        Avatar oldAvatar = avatarRepository.findByStudentId(studentId);
        if (oldAvatar != null) {
            logger.info("Avatar already exists, deleting outdated avatar");
            Files.delete(Path.of(oldAvatar.getFilePath()));
            avatar.setId(oldAvatar.getId());
        }
        return avatarRepository.save(avatar).getId();
    }

    private String getExtension(String string) {
        logger.info("Method getExtension was invoked");
        return string.substring(string.lastIndexOf("."));
    }

    private Path saveAvatarLocal(MultipartFile file) throws IOException {
        logger.info("Method saveAvatarLocal was invoked");
        createDirectoryIfNotExists();

        if (file.getOriginalFilename() == null) {
            logger.error("Invalid image");
            throw new RuntimeException("Некорректное изображение");
        }
        Path path = Path.of(pathDir.toString(), UUID.randomUUID() + getExtension(file.getOriginalFilename()));

        try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(file.getBytes())); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            bis.transferTo(bos);
        }
        return path;
    }

    private void createDirectoryIfNotExists() {
        logger.info("Method createDirectoryIfExists was invoked");
        try {
            if (Files.notExists(Path.of(pathDir.toString()))) {
                logger.debug("Directory does not exist, creating a directory");
                Files.createDirectory(Path.of(pathDir.toString()));
            }
        } catch (IOException e) {
            logger.error("Failed to create avatar directory");
            throw new RuntimeException("Не удалось создать директорию для аватаров");
        }
    }

    public Avatar getAvatarFromDb(long studentId) {
        logger.info("Method getAvatarFromDb was invoked");
        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            logger.error("Avatar for student with id: {} not found", studentId);
            throw new AvatarNotFoundException("Аватар не найден");
        }
        return avatar;
    }

    public AvatarView getAvatarFromDirectory(long studentId) {
        logger.info("Method getAvatarFromDirectory was invoked");
        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            logger.error("Avatar for student with id: {} not found", studentId);
            throw new AvatarNotFoundException("Аватар не найден");
        }
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Path.of(avatar.getFilePath()));
        } catch (IOException e) {
            String errorMessage = String.format("Не удалось прочитать файл с изображением по пути: %s. %s", avatar.getFilePath(), e.getMessage());
            logger.error("Failed to read image file");
            throw new ImageReadFailureException(errorMessage, e);
        }
        return new AvatarView(MediaType.parseMediaType(avatar.getMediaType()), bytes);
    }

    public void setPathDir(Path pathDir) {
        logger.info("Method setPathDir was invoked");
        this.pathDir = pathDir;
    }

    public List<Avatar> getAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Method getAvatars was invoked");
        if (pageSize > 0 && pageNumber > 0) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
            return avatarRepository.findAll(pageRequest).getContent();
        } else {
            logger.error("Invalid page parameters");
            throw new IllegalArgumentException("Переданы некорректные параметры страницы");
        }
    }
}
