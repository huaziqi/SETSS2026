package com.LHZ.SETSS2026.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final String jokeUrl = "https://api.timelessq.com/joke";
    private final String gptUrl = "https://free.v36.cm";
    private final String embeddingUrl = "https://dashscope.aliyuncs.com/compatible-mode";
    @Bean("joke")
    @Primary
    public WebClient jokeClient(WebClient.Builder builder){
        return builder.baseUrl(jokeUrl).build();
    }
    @Bean("gptChat")
    public WebClient gptClient(WebClient.Builder builder){
        return builder.baseUrl(gptUrl).build();
    }

    @Bean("embedding")
    public WebClient embeddingClient(WebClient.Builder builder){
        return builder.baseUrl(embeddingUrl).build();
    }

}
