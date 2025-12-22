package com.mobilsec.analysis.web.dto;

import java.time.Instant;
import java.util.List;

import com.mobilsec.analysis.service.ManifestFlags;
import com.mobilsec.analysis.service.ExportedComponent;

public record AnalysisDetailDto(
        Long id,
        String packageName,
        String versionName,
        String riskLevel,
        Instant createdAt,
        List<String> permissions,
        ManifestFlags manifestFlags,
        List<ExportedComponent> exportedComponents) {
}
