package com.mobilsec.analysis.web.dto;

import java.time.Instant;

import com.mobilsec.analysis.service.ApkMetadata;

public record UploadResponse(
        Long id,
        String fileName,
        Instant receivedAt,
        String riskLevel,
        ApkMetadata metadata) {
}
