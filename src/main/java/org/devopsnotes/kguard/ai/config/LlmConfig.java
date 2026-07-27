package org.devopsnotes.kguard.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    @Bean
    public RestClient ollamaRestClient(LlmProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeoutSeconds() * 1000);
        requestFactory.setReadTimeout(properties.timeoutSeconds() * 1000);

        return RestClient.builder()
                .baseUrl(Objects.requireNonNull(properties.baseUrl(), "kguard.ai.llm.base-url must not be null"))
                .requestFactory(requestFactory)
                .build();
    }
}
