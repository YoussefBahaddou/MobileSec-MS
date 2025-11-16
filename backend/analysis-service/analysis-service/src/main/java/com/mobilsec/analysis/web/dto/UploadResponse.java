package com.mobilsec.analysis.web.dto;

import java.time.Instant;

public record UploadResponse(String fileName, String status, Instant receivedAt) {
}
