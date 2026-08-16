package com.llmrouter.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Configuration
public class HttpClientConfiguration {

    @Bean
    RestClientCustomizer providerTimeoutCustomizer() {
        return builder -> {
            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
            requestFactory.setReadTimeout(Duration.ofSeconds(60));
            builder.requestFactory(requestFactory);
        };
    }
}
