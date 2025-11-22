package com.mobilesec.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResultDto(
        Long id,
        String packageName,
        String versionName,
        RiskLevel riskLevel,
        List<String> riskReasons,
        List<String> permissions,
        ManifestFlags manifestFlags,
        List<ExportedComponent> exportedComponents,
        Instant createdAt
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ManifestFlags(
            boolean debuggable,
            boolean allowBackup,
            boolean cleartextTrafficPermitted
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExportedComponent(
            String name,
            String type,
            boolean exported,
            String permission,
            List<String> intentFilters
    ) {
    }
}
