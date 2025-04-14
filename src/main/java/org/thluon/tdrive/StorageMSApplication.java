package org.thluon.tdrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.thluon.tdrive.config.FileServiceConf;

@SpringBootApplication
@EnableWebFlux
@EnableConfigurationProperties(FileServiceConf.class)
public class StorageMSApplication {
    public static void main(String[] args) {
        SpringApplication.run(StorageMSApplication.class,args);
    }
}