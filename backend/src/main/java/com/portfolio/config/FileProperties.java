package com.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "file")
@Component
@Getter
@Setter
public class FileProperties {
    private String uploadDir;
    private String galleryDir;
    private String freeDir;
}
