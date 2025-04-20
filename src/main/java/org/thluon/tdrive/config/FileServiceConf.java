package org.thluon.tdrive.config;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file_service")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileServiceConf {
  String uploadDirectory = "storage";

  @Bean
  String uploadDirectory() {
    return uploadDirectory;
  }

  @Bean
  FileSystem fileSystem() {
    return FileSystems.getDefault();
  }
}
