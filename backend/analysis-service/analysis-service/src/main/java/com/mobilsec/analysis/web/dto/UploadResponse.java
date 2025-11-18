package com.mobilsec.analysis.web.dto;

import java.time.Instant;
import java.util.List;

import com.mobilsec.analysis.service.ApkMetadata;

public record UploadResponse(
        Long id,
        String fileName,
        Instant receivedAt,
        String riskLevel,
        List<String> riskReasons,
        ApkMetadata metadata) {
}
