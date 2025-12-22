package com.mobilesec.fixsuggest.service;

import com.mobilesec.fixsuggest.dto.FixRequest;
import com.mobilesec.fixsuggest.dto.FixResponse;
import com.mobilesec.fixsuggest.model.FixRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FixServiceImpl implements FixService {

    private final RuleEngine ruleEngine;
    private final AiPatchService aiPatchService;

    @Override
    public FixResponse generateFix(FixRequest request) {
        // 1. Try Static Rule Engine
        FixRule rule = ruleEngine.findRule(request.getIssueId());
        if (rule != null) {
            return FixResponse.builder()
                    .fixId("FIX-" + rule.getId())
                    .title("Static Fix Available")
                    .explanation(rule.getExplanation())
                    .codeFix(rule.getFixCode())
                    .type("STATIC_RULE")
                    .confidenceScore(100)
                    .build();
        }

        // 2. AI Fallback
        return aiPatchService.generatePatch(
                request.getDescription(),
                request.getVulnerableCode() != null ? request.getVulnerableCode() : "// No code provided");
    }
}
