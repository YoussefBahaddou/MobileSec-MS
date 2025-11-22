package com.mobilsec.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskScorerTest {

    private RiskScorer riskScorer;

    @BeforeEach
    void setUp() {
        riskScorer = new RiskScorer();
    }

    @Test
    void highRiskWhenDangerousPermissionPresent() {
        ApkMetadata metadata = new ApkMetadata(
                "com.example.high",
                "1.0",
                List.of("android.permission.READ_SMS"),
                new ManifestFlags(false, false, false),
                List.of(),
                List.of(),
                List.of());

        RiskAssessment assessment = riskScorer.score(metadata);

        assertThat(assessment.level()).isEqualTo(RiskLevel.HIGH);
        assertThat(assessment.reasons()).containsExactly("permission=android.permission.READ_SMS");
    }

    @Test
    void mediumRiskWhenLocationAndBackupEnabled() {
        ApkMetadata metadata = new ApkMetadata(
                "com.example.medium",
                "1.0",
                List.of("android.permission.ACCESS_FINE_LOCATION"),
                new ManifestFlags(false, true, false),
                List.of(),
                List.of(),
                List.of());

        RiskAssessment assessment = riskScorer.score(metadata);

        assertThat(assessment.level()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(assessment.reasons()).containsExactly("allowBackup=true",
                "permission=android.permission.ACCESS_FINE_LOCATION");
    }

    @Test
    void lowRiskWhenNoTriggers() {
        ApkMetadata metadata = new ApkMetadata(
                "com.example.low",
                "1.0",
                List.of("android.permission.INTERNET"),
                new ManifestFlags(false, false, false),
                List.of(),
                List.of(),
                List.of());

        RiskAssessment assessment = riskScorer.score(metadata);

        assertThat(assessment.level()).isEqualTo(RiskLevel.LOW);
        assertThat(assessment.reasons()).isEmpty();
    }
}
