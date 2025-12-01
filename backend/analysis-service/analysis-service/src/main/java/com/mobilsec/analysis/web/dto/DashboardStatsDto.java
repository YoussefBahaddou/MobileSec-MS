package com.mobilsec.analysis.web.dto;

import java.util.List;

public record DashboardStatsDto(
        long totalScans,
        long highRiskCount,
        long mediumRiskCount,
        long lowRiskCount,
        List<AnalysisResultDto> recentScans) {
}
