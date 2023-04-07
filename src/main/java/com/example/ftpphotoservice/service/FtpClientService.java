package com.example.ftpphotoservice.service;


import com.example.ftpphotoservice.factory.FtpClientFactory;
import com.example.ftpphotoservice.model.Photo;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс FtpClientService представляет сервис для работы с FTP-сервером.
 * Он содержит методы connectToFtpServer() (подключение к FTP-серверу),
 * disconnectFromFtpServer() (отключение от FTP-сервера),
 * listFiles() (получение списка файлов с FTP-сервера)
 */

@Service
@Slf4j
public class FtpClientService {
    private final FtpClientFactory ftpClientFactory;
    private FTPClient ftpClient;

    @Autowired
    public FtpClientService(FtpClientFactory ftpClientFactory) {
        this.ftpClientFactory = ftpClientFactory;
    }

    /**
     * Метод для подключения к FTP-серверу.
     * Использует ftpClientFactory для создания экземпляра FTPClient.
     */
    public void connectToFtpServer() {
        log.info("Подключение к FTP-серверу");
        ftpClient = ftpClientFactory.createFtpClient();
    }

    /**
     * Метод для отключения от FTP-сервера.
     * Проверяет, подключен ли клиент, и выполняет отключение.
     * В случае ошибки, логирует сообщение об ошибке.
     */
    public void disconnectFromFtpServer() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                log.info("Отключен от FTP-сервера");
            } catch (IOException e) {
                log.error("Не удалось отключить", e);
            }
        }
    }

    /**
     * Метод для получения списка файлов с FTP-сервера.
     * Рекурсивно обходит директории, начиная с указанной родительской директории,
     * и возвращает список файлов, соответствующих заданным критериям.
     *
     * @param ftpDirectory     Родительская директория на FTP-сервере
     * @param targetFolderName Имя целевой папки, содержащей файлы, которые нужно получить
     * @param fileNamePrefix   Префикс имени файлов, которые нужно получить
     * @return Список объектов класса Photo, представляющих файлы на FTP-сервере
     */
    public List<Photo> listFiles(String ftpDirectory, String targetFolderName, String fileNamePrefix) {
        List<Photo> photoList = new ArrayList<>();
        try {
            FTPFile[] files = ftpClient.listFiles(ftpDirectory);
            for (FTPFile file : files) {
                if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                    if (file.getName().equals(targetFolderName)) {
                        Path photoFolderPath = Path.of(ftpDirectory, file.getName());
                        FTPFile[] photos = ftpClient.listFiles(photoFolderPath.toString());
                        for (FTPFile photo : photos) {
                            if (photo.getName().startsWith(fileNamePrefix)) {
                                Photo photoObject = createPhotoObject(photo, photoFolderPath.toString());
                                photoList.add(photoObject);
                            }
                        }
                    }

                    photoList.addAll(listFiles(new String((ftpDirectory + "/" + file.getName()).getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.US_ASCII), targetFolderName, fileNamePrefix));
                }
            }
        } catch (IOException e) {
            log.error("Ошибка при получении списка файлов", e);
            // или выбросить более информативное исключение с сообщением об ошибке
        }
        return photoList;
    }

    /**
     * Вспомогательный метод для создания объекта Photo на основе FTPFile и пути к папке.
     *
     * @param ftpFile         Объект FTPFile, представляющий файл на FTP-сервере.
     * @param photoFolderPath Путь к папке, содержащей файл на FTP-сервере.
     * @return Объект Photo, представляющий файл на FTP-сервере.
     */
    private Photo createPhotoObject(FTPFile ftpFile, String photoFolderPath) {
        Photo photo = new Photo();
        photo.setName(ftpFile.getName());
        photo.setPath(photoFolderPath);
        photo.setCreationTime(ftpFile.getTimestamp().getTime());
        photo.setSize(ftpFile.getSize());
        return photo;
    }
}
