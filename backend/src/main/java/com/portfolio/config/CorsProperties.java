package com.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cors")
@Component
@Getter
@Setter
public class CorsProperties {
    private String allowedOrigins;
}
