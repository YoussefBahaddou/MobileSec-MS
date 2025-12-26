package com.mobilsec.analysis.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobilsec.analysis.service.RiskLevel;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findTopByPackageNameOrderByCreatedAtDesc(String packageName);

    long countByRiskLevel(String riskLevel);
}
