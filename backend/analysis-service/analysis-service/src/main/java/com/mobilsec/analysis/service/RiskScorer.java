package com.mobilsec.analysis.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    public RiskAssessment score(ApkMetadata metadata) {
        List<String> reasons = new ArrayList<>();

        if (metadata.manifestFlags().debuggable()) {
            reasons.add("debuggable=true");
        }

        if (metadata.manifestFlags().cleartextTrafficPermitted()) {
            reasons.add("cleartextTrafficPermitted=true");
        }

        Set<String> highRiskMatched = new LinkedHashSet<>();
        for (String permission : metadata.permissions()) {
            if (HIGH_RISK_PERMISSIONS.contains(permission)) {
                highRiskMatched.add(permission);
            }
        }
        highRiskMatched.stream()
                .map(perm -> "permission=" + perm)
                .forEach(reasons::add);

        if (!reasons.isEmpty()) {
            return new RiskAssessment(RiskLevel.HIGH, List.copyOf(reasons));
        }

        List<String> mediumReasons = new ArrayList<>();
        if (metadata.manifestFlags().allowBackup()) {
            mediumReasons.add("allowBackup=true");
        }

        metadata.permissions().stream()
                .filter(LOCATION_PERMISSIONS::contains)
                .distinct()
                .map(perm -> "permission=" + perm)
                .forEach(mediumReasons::add);

        if (!mediumReasons.isEmpty()) {
            return new RiskAssessment(RiskLevel.MEDIUM, List.copyOf(mediumReasons));
        }

        return new RiskAssessment(RiskLevel.LOW, List.of());
    }
}
