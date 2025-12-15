package com.mobilesec.fixsuggest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.fixsuggest.model.FixRule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleEngine {

    private final Map<String, FixRule> ruleCache = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void loadRules() {
        try (InputStream is = new ClassPathResource("masvs-rules.json").getInputStream()) {
            List<FixRule> rules = mapper.readValue(is, new TypeReference<>() {
            });
            ruleCache.putAll(rules.stream().collect(Collectors.toMap(FixRule::getId, r -> r)));
            log.info("Loaded {} static security rules.", ruleCache.size());
        } catch (Exception e) {
            log.error("Failed to load static rules: {}", e.getMessage());
        }
    }

    public FixRule findRule(String issueId) {
        return ruleCache.get(issueId);
    }
}
