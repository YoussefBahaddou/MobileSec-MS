package com.mobilsec.analysis.web;

import java.time.Instant;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobilsec.analysis.persistence.AnalysisResult;
import com.mobilsec.analysis.persistence.AnalysisResultRepository;
import com.mobilsec.analysis.web.dto.UploadResponse;

@RestController
@RequestMapping("/analysis")
public class UploadController {

    private final AnalysisResultRepository repository;

    public UploadController(AnalysisResultRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/upload")
    public UploadResponse uploadApk(@RequestParam("file") MultipartFile file) {
        Instant receivedAt = Instant.now();
        AnalysisResult result = new AnalysisResult(
                file.getOriginalFilename(),
                "LOW",
                receivedAt);

        repository.save(result);

        return new UploadResponse(
                file.getOriginalFilename(),
                "SAVED",
                receivedAt);
    }
}
