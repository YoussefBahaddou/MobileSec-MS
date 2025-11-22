package com.mobilesec.reportservice.rendering;

import java.util.Locale;

public enum ReportFormat {
    JSON(false, "application/json"),
    SARIF(false, "application/json"),
    PDF(true, "application/pdf");

    private final boolean binary;
    private final String mediaType;

    ReportFormat(boolean binary, String mediaType) {
        this.binary = binary;
        this.mediaType = mediaType;
    }

    public boolean isBinary() {
        return binary;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static ReportFormat fromParam(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }
        try {
            return ReportFormat.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported report format: " + value + ". Allowed values are JSON, SARIF, PDF.");
        }
    }
}
