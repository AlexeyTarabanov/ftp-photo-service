package com.example.ftpphotoservice.factory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * Класс FtpClientFactory представляет фабрику для создания экземпляра FTP-клиента.
 * Он содержит поля для настроек подключения к FTP-серверу
 * (считываемых из конфигурационных файлов с использованием аннотации @Value),
 * и метод createFtpClient() для создания и настройки экземпляра FTP-клиента
 */
@Service
@Slf4j
public class FtpClientFactory {
    @Value("${spring.ftp.server}")
    private String server;
    @Value("${spring.ftp.port}")
    private int port;
    @Value("${spring.ftp.user}")
    private String user;
    @Value("${spring.ftp.password}")
    private String password;

    public FTPClient createFtpClient() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, password);

            // Установка кодировки для команды управления
            ftpClient.setControlEncoding("UTF-8");
            // Установка кодировки символов для обработки файлов на FTP-сервере
            ftpClient.setCharset(StandardCharsets.UTF_8);
            // Установка пассивного режима передачи данных
            ftpClient.enterLocalPassiveMode();
            // Установка типа передачи файла как бинарный
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            log.info("FTP-клиент создан успешно");

        } catch (IOException e) {
            log.error("Не удалось создать FTP-клиент", e);
            //throw new IOException("Не удалось создать FTP-клиент. Подробности: " + e.getMessage(), e);
        }

        return ftpClient;
    }
}
