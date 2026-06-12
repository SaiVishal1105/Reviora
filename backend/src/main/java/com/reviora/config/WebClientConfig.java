package com.reviora.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {

        try {

            SslContextBuilder sslContextBuilder =
                    SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE);

            HttpClient httpClient = HttpClient.create()
                    .secure(ssl ->
                            ssl.sslContext(sslContextBuilder));

            return WebClient.builder()
                    .clientConnector(
                            new ReactorClientHttpConnector(httpClient)
                    );

        } catch (Exception e) {

            log.error("Failed to create insecure WebClient", e);

            return WebClient.builder();
        }
    }
}