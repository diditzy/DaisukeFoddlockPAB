package com.example.daisukefoddlock.config;

import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.service.MidtransSnapApi;
import com.midtrans.service.MidtransCoreApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MidtransConfig {

    private final MidtransProperties properties;

    @Bean
    public Config midtransConfig() {
        return Config.builder()
                .setServerKey(properties.getServerKey())
                .setClientKey(properties.getClientKey())
                .setIsProduction(properties.isProduction())
                .build();
    }

    @Bean
    public MidtransSnapApi midtransSnapApi(Config config) {
        return new ConfigFactory(config).getSnapApi();
    }

    @Bean
    public MidtransCoreApi midtransCoreApi(Config config) {
        return new ConfigFactory(config).getCoreApi();
    }
}
