package com.mobilsec.analysis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class NetworkScannerService {

    private static final Pattern HTTP_URL = Pattern.compile("http://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}");
    private static final Pattern USE_CLEARTEXT_TRAFFIC = Pattern
            .compile("android:usesCleartextTraffic\\s*=\\s*\"true\"");

    public List<String> scan(String content) {
        List<String> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return findings;
        }

        checkPattern(content, HTTP_URL, "Insecure HTTP URL found", findings);

        // This check is redundant if we parse manifest flags, but good as a fallback or
        // for other files
        // checkPattern(content, USE_CLEARTEXT_TRAFFIC, "Cleartext Traffic Explicitly
        // Allowed", findings);

        return findings;
    }

    private void checkPattern(String content, Pattern pattern, String message, List<String> findings) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            // Add the specific match to the finding for context
            String match = matcher.group();
            if (!findings.contains(message + ": " + match)) {
                findings.add(message + ": " + match);
            }
        }
    }
}
