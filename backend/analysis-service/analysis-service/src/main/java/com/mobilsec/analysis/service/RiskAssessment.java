package com.mobilsec.analysis.service;

import java.util.List;
import java.util.Map;

public record RiskAssessment(RiskLevel level, java.util.List<String> reasons, Map<String, String> remediation) {
}
