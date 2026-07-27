package org.devopsnotes.kguard.ai.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LlmProperties.class, ElasticsearchProperties.class})
public class ElasticsearchConfig {

    @Bean
    @ConditionalOnProperty(prefix = "kguard.ai.elasticsearch", name = "export-enabled", havingValue = "true")
    public RestClient elasticRestClient(ElasticsearchProperties properties) {
        return RestClient.builder(org.apache.http.HttpHost.create(properties.url())).build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "kguard.ai.elasticsearch", name = "export-enabled", havingValue = "true")
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    @ConditionalOnProperty(prefix = "kguard.ai.elasticsearch", name = "export-enabled", havingValue = "true")
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
