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
            java.io.File tempFile = null;
            try {
                // Save to temp file to handle large uploads without OOM
                java.nio.file.Path tempPath = java.nio.file.Files.createTempFile("crypto-scan-",
                        file.getOriginalFilename());
                tempFile = tempPath.toFile();
                file.transferTo(tempFile);

                List<CryptoFinding> findings = scannerService.scanFile(tempFile, file.getOriginalFilename());

                // Cleanup
                java.nio.file.Files.deleteIfExists(tempPath);

                return ResponseEntity.ok(Map.of(
                        "status", "completed",
                        "findings_count", findings.size(),
                        "findings", findings));

            } catch (IOException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to process file: " + e.getMessage()));
            }
        } else if (code != null) {
            List<CryptoFinding> findings = scannerService.scanCode(code, "input.java");
            return ResponseEntity.ok(Map.of(
                    "status", "completed",
                    "findings_count", findings.size(),
                    "findings", findings));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "No code or file provided"));
        }

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "findings_count", findings.size(),
                "findings", findings));
    }
}
