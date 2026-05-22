package com.uees.studentservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final AppProperties props;

    public WebClientConfig(AppProperties props) {
        this.props = props;
    }

    @Bean(name = "learningEngineWebClient")
    public WebClient learningEngineWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(props.getLearningEngine().getBaseUrl())
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean(name = "espoCrmWebClient")
    public WebClient espoCrmWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(props.getEspocrm().getBaseUrl())
                .defaultHeader("Accept", "application/json")
                .defaultHeader("X-Api-Key", props.getEspocrm().getApiKey())
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
