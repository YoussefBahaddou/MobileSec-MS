package com.mobilesec.cryptocheck.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CryptoFinding {
    private String ruleId;
    private String description;
    private String severity; // HIGH, MEDIUM, LOW
    private String fileName;
    private int lineNumber;
    private String snippet;
}
