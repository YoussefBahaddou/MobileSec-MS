package com.mobilesec.fixsuggest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixResponse {
    private String fixId; // unique ID of the suggestion
    private String title; // "Disable Debugging"
    private String explanation; // "Application is debuggable..."
    private String codeFix; // Concrete code: "android:debuggable=\"false\""
    private String type; // "STATIC_RULE" or "AI_GENERATED"
    private int confidenceScore; // 100 for static, variable for AI
}
