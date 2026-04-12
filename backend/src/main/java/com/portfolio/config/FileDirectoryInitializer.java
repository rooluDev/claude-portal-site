package com.portfolio.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDirectoryInitializer implements ApplicationRunner {

    private final FileProperties fileProperties;

    @Override
    public void run(ApplicationArguments args) {
        createDirectory(fileProperties.getUploadDir());
        createDirectory(fileProperties.getGalleryDir());
        createDirectory(fileProperties.getFreeDir());
    }

    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("디렉토리 생성: {}", dir.getAbsolutePath());
            }
        }
    }
}
