package com.example.ftpphotoservice.model;

import lombok.Data;

import java.util.Date;
import java.util.Objects;


/**
 * Класс Photo представляет сущность "Фотография" со свойствами
 * name (имя), path (путь), creationTime (время создания) и size (размер)
 */
@Data
public class Photo {
    private String name;
    private String path;
    private Date creationTime;
    private long size;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return size == photo.size && Objects.equals(name, photo.name) && Objects.equals(path, photo.path)
                && Objects.equals(creationTime, photo.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, creationTime, size);
    }
}
