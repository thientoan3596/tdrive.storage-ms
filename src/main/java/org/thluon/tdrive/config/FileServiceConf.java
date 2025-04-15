package org.thluon.tdrive.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Configuration
@ConfigurationProperties(prefix = "file-service.conf")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileServiceConf {
    String uploadDirectory = "storage";
    @Bean
    public String uploadDirectory() {
        return uploadDirectory;
    }
    @Bean
    public FileSystem fileSystem() {
        return FileSystems.getDefault();
    }
}
