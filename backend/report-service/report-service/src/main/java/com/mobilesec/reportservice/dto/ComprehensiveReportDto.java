package com.mobilesec.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComprehensiveReportDto {
    private String scanId;
    private ManifestResult manifest;
    private SecretResult secrets;
    private CryptoResult crypto;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ManifestResult {
        @com.fasterxml.jackson.annotation.JsonProperty("package_name")
        private String packageName;

        @com.fasterxml.jackson.annotation.JsonProperty("version_code")
        private String versionCode;

        @com.fasterxml.jackson.annotation.JsonProperty("is_debuggable")
        private boolean debuggable;

        @com.fasterxml.jackson.annotation.JsonProperty("allow_backup")
        private boolean allowBackup;

        @com.fasterxml.jackson.annotation.JsonProperty("uses_cleartext_traffic")
        private boolean usesCleartextTraffic;

        private List<String> permissions;

        @com.fasterxml.jackson.annotation.JsonProperty("exported_activities")
        private List<String> exportedActivities;

        @com.fasterxml.jackson.annotation.JsonProperty("exported_services")
        private List<String> exportedServices;

        @com.fasterxml.jackson.annotation.JsonProperty("exported_receivers")
        private List<String> exportedReceivers;

        @com.fasterxml.jackson.annotation.JsonProperty("exported_providers")
        private List<String> exportedProviders;
    }

    // We assume the frontend calculates score or backend does.
    // For simplicity, we calculate score in backend service.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SecretResult {
        private List<SecretFinding> findings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SecretFinding {
        private String type;
        private String match;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CryptoResult {
        private List<CryptoFinding> findings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CryptoFinding {
        private String ruleId;
        private String description;
        private String severity;
        private String location;
    }
}
