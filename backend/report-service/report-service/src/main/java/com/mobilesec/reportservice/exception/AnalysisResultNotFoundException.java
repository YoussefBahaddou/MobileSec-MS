package com.mobilesec.reportservice.exception;

public class AnalysisResultNotFoundException extends RuntimeException {

    public AnalysisResultNotFoundException(String message) {
        super(message);
    }

    public AnalysisResultNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
