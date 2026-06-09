package com.denizlektemur.steamachievementtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SteamApiConfig {

    @Value("${steam.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient steamRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}