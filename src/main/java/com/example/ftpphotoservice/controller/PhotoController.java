package com.example.ftpphotoservice.controller;

import com.example.ftpphotoservice.model.Photo;
import com.example.ftpphotoservice.service.PhotoService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Класс PhotoController представляет контроллер для взаимодействия с фотографиями через REST API.
 * Он содержит метод getPhotos() для получения списка фотографий, используя PhotoService
 * и метод getPhotoInfo(),
 * который позволяет получить подробную информацию о фото по указанному пути к файлу в запросе.
 */
@RestController
@RequestMapping("/photos")
@Slf4j
public class PhotoController {
    private final PhotoService photoService;


    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public List<Photo> getPhotos() {
        return photoService.getPhotos();
    }

    @GetMapping("/photo")
    public Photo getPhotoInfo(@RequestParam("path") String filePath) {
        return photoService.getPhotoInfo(filePath);
    }

    @PreDestroy
    public void destroy() {
        try {
            photoService.disconnect();
        } catch (Exception e) {
            log.error("Ошибка при закрытии соединения с FTP-сервером: {}", e.getMessage());
        }
    }
}

