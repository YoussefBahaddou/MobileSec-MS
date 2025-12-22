package com.mobilsec.analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class CryptoScannerService {

    private static final Pattern WEAK_HASH_MD5 = Pattern.compile("MD5");
    private static final Pattern WEAK_HASH_SHA1 = Pattern.compile("SHA-1");
    private static final Pattern INSECURE_MODE_ECB = Pattern.compile("AES/ECB");
    private static final Pattern INSECURE_MODE_DES = Pattern.compile("DES");
    private static final Pattern HARDCODED_IV = Pattern.compile("IvParameterSpec\\s*\\(\\s*new\\s+byte\\[\\]");

    public List<String> scan(String content) {
        List<String> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return findings;
        }

        checkPattern(content, WEAK_HASH_MD5, "Weak Hashing Algorithm (MD5) detected", findings);
        checkPattern(content, WEAK_HASH_SHA1, "Weak Hashing Algorithm (SHA-1) detected", findings);
        checkPattern(content, INSECURE_MODE_ECB, "Insecure Encryption Mode (AES/ECB) detected", findings);
        checkPattern(content, INSECURE_MODE_DES, "Obsolete Encryption Algorithm (DES) detected", findings);
        checkPattern(content, HARDCODED_IV, "Hardcoded Initialization Vector (IV) detected", findings);

        return findings;
    }

    private void checkPattern(String content, Pattern pattern, String message, List<String> findings) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            findings.add(message);
        }
    }
}
