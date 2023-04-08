package com.example.ftpphotoservice.service;

import com.example.ftpphotoservice.model.Photo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Класс PhotoService представляет сервис для работы с фотографиями.
 * Он содержит методы для получения списка фотографий с FTP-сервера, отключения от FTP-сервера,
 * и получения подробной информации о фотографии по ее пути.
 */
@Service
@Slf4j
public class PhotoService {

    private final FtpClientService ftpClientService;
    private static final String PHOTO_FOLDER = "фотографии";
    private static final String FILE_PREFIX = "GRP327_";

    @Autowired
    public PhotoService(FtpClientService ftpClientService) {
        this.ftpClientService = ftpClientService;
    }

    public List<Photo> getPhotos() {
        log.info("Начало получение фотографий с FTP-сервера");
        try {
            ftpClientService.connectToFtpServer();
            List<Photo> photos = new ArrayList<>();
            ftpClientService.listFiles("/", PHOTO_FOLDER, FILE_PREFIX, photos);
            log.info("Завершение получения фотографий с FTP-сервера. Всего найдено фотографий: {}", photos.size());
            if (photos.isEmpty()) {
                throw new RuntimeException("Ошибка при получении фотографий с FTP-сервера: список файлов пуст");
            }
            return photos;
        } catch (Exception e) {
            log.error("Ошибка при подключении к FTP-серверу: {}", e.getMessage());
            // выбрасываем исключение дальше, если нужно
            throw e;
        } finally {
            // закрываем соединение с FTP-сервером в любом случае
            ftpClientService.disconnectFromFtpServer();
        }
    }

    public void disconnect() {
        if (ftpClientService != null) {
            ftpClientService.disconnectFromFtpServer();
        }
    }

    public Photo getPhotoInfo(String path) {
        // Создаем объект класса File на основе указанного пути
        File file = new File(path);

        // Проверяем, существует ли файл
        if (!file.exists()) {
            log.error("Файл не найден: {}", path);
            return null;
        }

        // Получаем информацию о файле
        String name = file.getName();
        long size = file.length();
        Date creationTime = new Date(file.lastModified());

        // Возвращаем объект с подробной информацией о фотографии
        return new Photo(name, path, creationTime, size);
    }
}
