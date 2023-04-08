package com.example.ftpphotoservice.service;


import com.example.ftpphotoservice.factory.FtpClientFactory;
import com.example.ftpphotoservice.model.Photo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
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
     * @param ftpDirectory Родительская директория на FTP-сервере
     * @param targetFolderName Имя целевой папки, содержащей файлы, которые нужно получить
     * @param fileNamePrefix Префикс имени файлов, которые нужно получить
     * @param photoList Список объектов класса Photo, в который будут добавлены найденные файлы
     */
    public void listFiles(String ftpDirectory, String targetFolderName, String fileNamePrefix, List<Photo> photoList) {
        try {
            String path = transformFtpDirectory(ftpDirectory);
            FTPFile[] files = ftpClient.listFiles(path);
            for (FTPFile file : files) {
                if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                    if (file.getName().equals(targetFolderName)) {
                        Path photoFolderPath = Path.of(ftpDirectory, file.getName());
                        path = transformFtpDirectory(photoFolderPath.toString());
                        FTPFile[] photos = ftpClient.listFiles(path);
                        for (FTPFile photo : photos) {
                            if (photo.getName().startsWith(fileNamePrefix)) {
                                Photo photoObject = createPhotoObject(photo, photoFolderPath.toString());
                                photoList.add(photoObject);
                            }
                        }
                    }

                    listFiles((ftpDirectory + "/" + decodeURL(file.getName())), targetFolderName, fileNamePrefix, photoList);
                }
            }
        } catch (IOException e) {
            log.error("Ошибка при получении списка файлов", e);
            // или выбросить более информативное исключение с сообщением об ошибке
        }
    }


    /**
     * Метод для преобразования строки с FTP-директорией в соответствии с заданными правилами:
     * 1. Если в FTP-директории присутствуют пробелы, то они удаляются.
     * 2. Если FTP-директория не содержит пробелов, то она преобразуется в строку, закодированную в кодировке KOI8-R.
     *
     * @param ftpDirectory FTP-директория
     * @return Преобразованная FTP-директория
     */
    private String transformFtpDirectory(String ftpDirectory) {
        String transformedPath = "";
        if (ftpDirectory.matches(".*\\s+.*")) {
            transformedPath = ftpDirectory.replaceAll("\\s+", "");
        } else {
            transformedPath = new String(ftpDirectory.getBytes(), Charset.forName("koi8-r"));
        }
        return transformedPath;
    }

    /**
     * Декодирует URL-строку из URL-кодированного формата в обычный текст,
     * используя кодировку UTF-8.
     *
     * @param encodedURL URL-строка в URL-кодированном формате для декодирования
     * @return Декодированная URL-строка в обычном текстовом формате
     */
    public String decodeURL(String encodedURL) {
        String decodedURL = null;
        try {
            decodedURL = URLDecoder.decode(encodedURL, StandardCharsets.UTF_8.toString());
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            log.error("Ошибка при декодировании URL: " + encodedURL, e);
        }
        return decodedURL;
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
        photo.setPath(decodeURL(photoFolderPath));
        photo.setCreationTime(ftpFile.getTimestamp().getTime());
        photo.setSize(ftpFile.getSize());
        return photo;
    }
}
