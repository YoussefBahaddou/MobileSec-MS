package com.mobilsec.analysis.web;

import org.springframework.data.domain.Page;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobilsec.analysis.service.AnalysisResultService;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;
import com.mobilsec.analysis.web.dto.AnalysisDetailDto;
import com.mobilsec.analysis.web.dto.AnalysisSummaryDto;

@RestController
@RequestMapping("/analysis/results")
public class AnalysisResultsController {

    private final AnalysisResultService resultService;

    public AnalysisResultsController(AnalysisResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public Page<AnalysisSummaryDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return resultService.list(page, size)
                .map(this::toSummaryDto);
    }

    @GetMapping("/{id}")
    public AnalysisDetailDto getById(@PathVariable Long id) {
        return toDetailDto(resultService.getById(id));
    }

    @GetMapping("/by-package/{packageName}")
    public AnalysisDetailDto getByPackage(@PathVariable String packageName) {
        return toDetailDto(resultService.getLatestByPackage(packageName));
    }

    private AnalysisSummaryDto toSummaryDto(AnalysisResultDto dto) {
        return new AnalysisSummaryDto(
                dto.id(),
                dto.packageName(),
                dto.riskLevel(),
                dto.createdAt());
    }

    private AnalysisDetailDto toDetailDto(AnalysisResultDto dto) {
        return new AnalysisDetailDto(
                dto.id(),
                dto.packageName(),
                dto.versionName(),
                dto.riskLevel(),
                dto.createdAt(),
                dto.permissions(),
                dto.manifestFlags(),
                dto.exportedComponents());
    }
}
