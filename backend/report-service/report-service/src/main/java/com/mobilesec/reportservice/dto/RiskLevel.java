package com.mobilesec.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum RiskLevel {
    UNKNOWN,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static RiskLevel fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return this == UNKNOWN ? "UNKNOWN" : name();
    }
}
