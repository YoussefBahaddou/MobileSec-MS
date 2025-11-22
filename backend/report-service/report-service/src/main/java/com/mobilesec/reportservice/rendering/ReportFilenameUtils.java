package com.mobilesec.reportservice.rendering;

import com.mobilesec.reportservice.dto.AnalysisResultDto;

final class ReportFilenameUtils {

    private static final String DEFAULT_IDENTIFIER = "unknown";

    private ReportFilenameUtils() {
    }

    static String composeFilename(AnalysisResultDto result, String extension) {
        return "analysis-report-" + identifier(result) + extension;
    }

    private static String identifier(AnalysisResultDto result) {
        if (result == null) {
            return DEFAULT_IDENTIFIER;
        }
        if (result.id() != null) {
            return sanitize(result.id().toString());
        }
        String packageName = result.packageName();
        if (packageName != null && !packageName.isBlank()) {
            return sanitize(packageName);
        }
        return DEFAULT_IDENTIFIER;
    }

    private static String sanitize(String input) {
        String sanitized = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? DEFAULT_IDENTIFIER : sanitized;
    }
}
