package com.example.ftpphotoservice.service;

import com.example.ftpphotoservice.model.Photo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Класс PhotoService представляет сервис для работы с фотографиями.
 * Он содержит метод getPhotos() для получения списка фотографий с FTP-сервера,
 * и метод disconnect() для отключения от FTP-сервера.
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
            FtpClientService ftpClient = ftpClientService;
            ftpClient.connectToFtpServer();
            List<Photo> photos = ftpClient.listFiles("/", PHOTO_FOLDER, FILE_PREFIX);
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
}
