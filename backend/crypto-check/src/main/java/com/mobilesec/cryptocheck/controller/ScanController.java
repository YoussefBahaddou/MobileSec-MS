package com.mobilesec.cryptocheck.controller;

import com.mobilesec.cryptocheck.model.CryptoFinding;
import com.mobilesec.cryptocheck.service.CryptoScannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class ScanController {

    private final CryptoScannerService scannerService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCode(@RequestParam(value = "code", required = false) String code,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String sourceCode = "";
        String fileName = "input.java";

        if (file != null) {
            try {
                sourceCode = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to read file"));
            }
        } else if (code != null) {
            sourceCode = code;
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "No code or file provided"));
        }

        List<CryptoFinding> findings = scannerService.scanCode(sourceCode, fileName);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "findings_count", findings.size(),
                "findings", findings));
    }
}
