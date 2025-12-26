package com.mobilesec.fixsuggest.service;

import com.mobilesec.fixsuggest.dto.FixResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class AiPatchService {

    private final WebClient webClient;

    @Value("${ai.api.key:DEMO_KEY}")
    private String apiKey;

    public AiPatchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    public FixResponse generatePatch(String issue, String codeSnippet) {
        log.info("Requesting AI Patch for issue: {}", issue);

        // 1. Construct Prompt
        String prompt = String.format(
                "You are a Senior Android Security Engineer. Fix the following vulnerability.\n" +
                        "Issue: %s\n" +
                        "Vulnerable Code:\n```java\n%s\n```\n" +
                        "Return ONLY the fixed code snippet.",
                issue, codeSnippet);

        // 2. Mock Response (for now, to avoid billing without key)
        // In real impl, we would call POST /chat/completions here.
        String mockedFix = "// AI Generated Safe Code\n// Corrected: " + issue + "\nsecureMethodCall();";

        return FixResponse.builder()
                .fixId("AI-" + System.currentTimeMillis())
                .title("AI Suggested Patch")
                .explanation("This patch replaces the vulnerable pattern with a secure alternative.")
                .codeFix(mockedFix)
                .type("AI_GENERATED")
                .confidenceScore(85)
                .build();
    }
}
