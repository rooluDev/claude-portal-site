package com.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "file-policy")
@Component
@Getter
@Setter
public class FilePolicyProperties {

    private BoardFilePolicy gallery = new BoardFilePolicy();
    private BoardFilePolicy free = new BoardFilePolicy();

    @Getter
    @Setter
    public static class BoardFilePolicy {
        private int maxCount;
        private int maxSizeMb;
        private String allowedExtensions;
    }
}
