package com.mobilesec.reportgen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("package_name")
        private String packageName;

        @JsonProperty("version_code")
        private String versionCode;

        @JsonProperty("is_debuggable")
        private boolean debuggable;

        @JsonProperty("allow_backup")
        private boolean allowBackup;

        @JsonProperty("uses_cleartext_traffic")
        private boolean usesCleartextTraffic;

        private List<String> permissions;
        private List<String> exportedActivities;
        private List<String> exportedServices;
        private List<String> exportedReceivers;
        private List<String> exportedProviders;

        @JsonProperty("security_score")
        private SecurityScore securityScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SecurityScore {
        private double score;

        @JsonProperty("risk_label")
        private String riskLabel;

        private String details;
    }

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
        private String fileName;
        private int lineNumber;
        private String snippet;
    }
}
