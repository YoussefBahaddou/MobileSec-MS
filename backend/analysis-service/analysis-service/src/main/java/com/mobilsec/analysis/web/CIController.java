package com.mobilsec.analysis.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilsec.analysis.service.ApkAnalysisService;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;
import com.mobilsec.analysis.web.dto.CIResponseDto;

@RestController
@RequestMapping("/ci")
public class CIController {

    private final ApkAnalysisService apkAnalysisService;

    public CIController(ApkAnalysisService apkAnalysisService) {
        this.apkAnalysisService = apkAnalysisService;
    }

    @PostMapping("/scan")
    public CIResponseDto scanForCI(@RequestParam("file") MultipartFile file) throws Exception {
        AnalysisResultDto result = apkAnalysisService.analyze(file);

        boolean passed = "LOW".equals(result.riskLevel()) || "MEDIUM".equals(result.riskLevel());
        // Fail on HIGH or CRITICAL

        int secretsCount = result.secrets() != null ? result.secrets().size() : 0;
        int cryptoCount = result.cryptoIssues() != null ? result.cryptoIssues().size() : 0;

        return new CIResponseDto(
                result.packageName(),
                result.versionName(),
                result.riskLevel(),
                passed,
                "http://localhost:3001/results/" + result.id(), // Hardcoded for MVP
                secretsCount,
                cryptoCount);
    }
}
