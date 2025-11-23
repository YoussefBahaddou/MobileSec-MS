package com.mobilsec.analysis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class RiskScorer {

    public RiskAssessment score(ApkMetadata metadata) {
        List<String> reasons = new ArrayList<>();
        Map<String, String> remediation = new HashMap<>();
        RiskLevel level = RiskLevel.LOW;

        // 1. Check Secrets (CRITICAL)
        if (metadata.secrets() != null && !metadata.secrets().isEmpty()) {
            level = RiskLevel.CRITICAL;
            reasons.add("Hardcoded secrets found: " + metadata.secrets().size());
            remediation.put("Hardcoded Secrets",
                    "Remove hardcoded secrets immediately. Use a secure vault or environment variables.");
        }

        // 2. Check Crypto Issues (HIGH)
        if (metadata.cryptoIssues() != null && !metadata.cryptoIssues().isEmpty()) {
            if (level.ordinal() < RiskLevel.HIGH.ordinal()) {
                level = RiskLevel.HIGH;
            }
            reasons.add("Weak cryptography detected: " + metadata.cryptoIssues().size());
            remediation.put("Weak Cryptography",
                    "Replace weak algorithms (MD5, SHA1, DES) with secure alternatives (SHA-256, AES-GCM).");
        }

        // 3. Check Network Issues (HIGH)
        if (metadata.networkIssues() != null && !metadata.networkIssues().isEmpty()) {
            if (level.ordinal() < RiskLevel.HIGH.ordinal()) {
                level = RiskLevel.HIGH;
            }
            reasons.add("Insecure network configuration detected: " + metadata.networkIssues().size());
            remediation.put("Network Security",
                    "Ensure all network traffic uses HTTPS. Disable cleartext traffic in network security config.");
        }

        // 4. Check Permissions (MEDIUM/HIGH)
        for (String permission : metadata.permissions()) {
            if (permission.equals("android.permission.READ_SMS")) {
                if (level.ordinal() < RiskLevel.HIGH.ordinal()) {
                    level = RiskLevel.HIGH;
                }
                reasons.add("permission=" + permission);
                remediation.put(permission, "This is a high-risk permission. Ensure it is absolutely necessary.");
            } else if (permission.equals("android.permission.ACCESS_FINE_LOCATION")) {
                if (level.ordinal() < RiskLevel.MEDIUM.ordinal()) {
                    level = RiskLevel.MEDIUM;
                }
                reasons.add("permission=" + permission);
                remediation.put(permission, "Consider using coarse location if fine location is not required.");
            } else if (permission.equals("android.permission.READ_EXTERNAL_STORAGE")) {
                if (level.ordinal() < RiskLevel.MEDIUM.ordinal()) {
                    level = RiskLevel.MEDIUM;
                }
                reasons.add("Requesting dangerous permission: READ_EXTERNAL_STORAGE");
                remediation.put("READ_EXTERNAL_STORAGE",
                        "Verify if this permission is strictly necessary. Use Scoped Storage if possible.");
            }
        }

        // Check allowBackup (MEDIUM)
        if (metadata.manifestFlags().allowBackup()) {
            if (level.ordinal() < RiskLevel.MEDIUM.ordinal()) {
                level = RiskLevel.MEDIUM;
            }
            reasons.add("allowBackup=true");
            remediation.put("allowBackup",
                    "Set android:allowBackup=\"false\" to prevent data extraction via ADB backup.");
        }

        // 5. Check Exported Components (MEDIUM)
        long exportedCount = metadata.exportedComponents().stream().filter(c -> c.exported()).count();
        if (exportedCount > 0) {
            if (level.ordinal() < RiskLevel.MEDIUM.ordinal()) {
                level = RiskLevel.MEDIUM;
            }
            reasons.add("Exported components found: " + exportedCount);
            remediation.put("Exported Components",
                    "Ensure exported components are protected by permissions or intents. Set android:exported=\"false\" if not needed.");
        }

        // 6. Check Debuggable (HIGH)
        if (metadata.manifestFlags().debuggable()) {
            if (level.ordinal() < RiskLevel.HIGH.ordinal()) {
                level = RiskLevel.HIGH;
            }
            reasons.add("App is debuggable");
            remediation.put("Debuggable",
                    "Set android:debuggable=\"false\" in AndroidManifest.xml for release builds.");
        }

        return new RiskAssessment(level, reasons, remediation);
    }
}
