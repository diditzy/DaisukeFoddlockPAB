package com.example.daisukefoddlock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "midtrans")
@Getter
@Setter
public class MidtransProperties {
    private String serverKey;
    private String clientKey;
    private String snapUrl;
    private boolean production;
}
