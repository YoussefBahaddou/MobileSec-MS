package com.mobilsec.analysis.service;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class RiskScorer {

    private static final Set<String> HIGH_RISK_PERMISSIONS = Set.of(
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.CALL_PHONE",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_SETTINGS",
            "android.permission.SYSTEM_ALERT_WINDOW");

    private static final Set<String> LOCATION_PERMISSIONS = Set.of(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION");

    public RiskLevel score(ApkMetadata metadata) {
        if (metadata.manifestFlags().debuggable()
                || metadata.manifestFlags().cleartextTrafficPermitted()
                || metadata.permissions().stream().anyMatch(HIGH_RISK_PERMISSIONS::contains)) {
            return RiskLevel.HIGH;
        }

        if (metadata.permissions().stream().anyMatch(LOCATION_PERMISSIONS::contains)
                || metadata.manifestFlags().allowBackup()) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }
}
