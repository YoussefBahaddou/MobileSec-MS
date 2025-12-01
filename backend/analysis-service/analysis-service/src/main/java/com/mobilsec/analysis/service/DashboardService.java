package com.mobilsec.analysis.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mobilsec.analysis.persistence.AnalysisResultRepository;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;
import com.mobilsec.analysis.web.dto.DashboardStatsDto;
import com.mobilsec.analysis.service.RiskLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnalysisResultRepository repository;
    private final AnalysisResultMapper mapper;

    public DashboardStatsDto getStats() {
        long totalScans = repository.count();
        long highRiskCount = repository.countByRiskLevel(RiskLevel.HIGH.name());
        long mediumRiskCount = repository.countByRiskLevel(RiskLevel.MEDIUM.name());
        long lowRiskCount = repository.countByRiskLevel(RiskLevel.LOW.name());

        List<AnalysisResultDto> recentScans = repository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(mapper::fromEntity)
                .collect(Collectors.toList());

        return new DashboardStatsDto(
                totalScans,
                highRiskCount,
                mediumRiskCount,
                lowRiskCount,
                recentScans);
    }
}
