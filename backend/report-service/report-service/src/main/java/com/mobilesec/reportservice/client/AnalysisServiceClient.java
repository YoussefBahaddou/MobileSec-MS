package com.mobilesec.reportservice.client;

import com.mobilesec.reportservice.dto.AnalysisResultDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AnalysisServiceClient {

    private final WebClient webClient;

    public AnalysisServiceClient(WebClient analysisWebClient) {
        this.webClient = analysisWebClient;
    }

    public Mono<AnalysisResultDto> fetchById(long id) {
        return webClient.get()
                .uri("/analysis/results/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AnalysisResultDto.class);
    }

    public Mono<AnalysisResultDto> fetchByPackage(String packageName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/analysis/results/by-package/{packageName}")
                        .build(packageName))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AnalysisResultDto.class);
    }
}
