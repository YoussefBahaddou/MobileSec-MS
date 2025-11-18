package com.mobilsec.analysis.web.dto;

import java.time.Instant;
import java.util.List;

import com.mobilsec.analysis.service.ExportedComponent;
import com.mobilsec.analysis.service.ManifestFlags;

public record AnalysisResultDto(
        Long id,
        String packageName,
        String versionName,
        String riskLevel,
        List<String> riskReasons,
        List<String> permissions,
        ManifestFlags manifestFlags,
        List<ExportedComponent> exportedComponents,
        Instant createdAt) {
}
