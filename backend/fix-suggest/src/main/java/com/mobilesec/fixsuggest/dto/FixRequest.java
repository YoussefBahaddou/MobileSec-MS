package com.mobilesec.fixsuggest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixRequest {
    private String issueId; // e.g., "ANDROID_DEBUGGABLE"
    private String description; // "App is debuggable"
    private String vulnerableCode; // The actual code snippet (optional)
    private String filePath; // "AndroidManifest.xml"
    private String severity; // "HIGH", "MEDIUM"
}
