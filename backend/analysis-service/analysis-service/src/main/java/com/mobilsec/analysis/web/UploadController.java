package com.mobilsec.analysis.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilsec.analysis.service.ApkAnalysisService;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;

@RestController
@RequestMapping("/analysis")
public class UploadController {

    private final ApkAnalysisService apkAnalysisService;

    public UploadController(ApkAnalysisService apkAnalysisService) {
        this.apkAnalysisService = apkAnalysisService;
    }

    @PostMapping("/upload")
    public AnalysisResultDto uploadApk(@RequestParam("file") MultipartFile file) throws Exception {
        return apkAnalysisService.analyze(file);
    }
}

