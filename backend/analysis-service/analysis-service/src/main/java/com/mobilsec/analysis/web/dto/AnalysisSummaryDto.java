package com.mobilsec.analysis.web.dto;

import java.time.Instant;

public record AnalysisSummaryDto(
        Long id,
        String packageName,
        String riskLevel,
        Instant createdAt) {
}
