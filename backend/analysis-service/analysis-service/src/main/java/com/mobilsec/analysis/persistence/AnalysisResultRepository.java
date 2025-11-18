package com.mobilsec.analysis.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findTopByPackageNameOrderByCreatedAtDesc(String packageName);
}
