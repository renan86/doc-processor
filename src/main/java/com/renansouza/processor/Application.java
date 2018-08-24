package com.renansouza.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.notExists;

@SpringBootApplication
@EnableBatchProcessing
@Slf4j
class Application {

    @Value("${com.renansouza.processor.file.upload:file/upload}")
    private String upload;

    @Value("${com.renansouza.processor.file.download:file/download}")
    private String download;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @PostConstruct
    void started() throws IOException {
        verifyDir(Paths.get(upload));
        verifyDir(Paths.get(download));
    }

    private void verifyDir(final Path dir) throws IOException {
        if (notExists(dir)) {
            log.info("Creating dir {}.", dir);
            Files.createDirectories(dir);
        }
    }

}