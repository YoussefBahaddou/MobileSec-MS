package com.mobilsec.analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class SecretScannerService {

    private static final Pattern AWS_ACCESS_KEY = Pattern.compile("AKIA[0-9A-Z]{16}");
    private static final Pattern GOOGLE_API_KEY = Pattern.compile("AIza[0-9A-Za-z\\-_]{35}");
    private static final Pattern GENERIC_PRIVATE_KEY = Pattern.compile("-----BEGIN PRIVATE KEY-----");
    private static final Pattern HARDCODED_PASSWORD = Pattern.compile("(?i)password\\s*=\\s*['\"][^'\"]+['\"]");
    private static final Pattern BEARER_TOKEN = Pattern.compile("Bearer\\s+[a-zA-Z0-9\\-\\._~\\+\\/]+=*");

    public List<String> scan(String content) {
        List<String> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return findings;
        }

        checkPattern(content, AWS_ACCESS_KEY, "AWS Access Key found", findings);
        checkPattern(content, GOOGLE_API_KEY, "Google API Key found", findings);
        checkPattern(content, GENERIC_PRIVATE_KEY, "Private Key found", findings);
        checkPattern(content, HARDCODED_PASSWORD, "Hardcoded Password found", findings);
        checkPattern(content, BEARER_TOKEN, "Bearer Token found", findings);

        return findings;
    }

    private void checkPattern(String content, Pattern pattern, String message, List<String> findings) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            findings.add(message);
        }
    }
}
