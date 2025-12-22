package com.mobilsec.analysis.web.dto;

public record CIResponseDto(
        String packageName,
        String version,
        String riskLevel,
        boolean passed,
        String reportUrl,
        int secretsFound,
        int cryptoIssuesFound) {
}
