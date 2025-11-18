package com.mobilsec.analysis.service;

import java.util.List;

public record RiskAssessment(RiskLevel level, List<String> reasons) {
}
