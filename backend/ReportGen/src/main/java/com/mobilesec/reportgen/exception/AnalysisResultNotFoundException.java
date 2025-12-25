package com.mobilesec.reportgen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AnalysisResultNotFoundException extends RuntimeException {

    public AnalysisResultNotFoundException(String message) {
        super(message);
    }

    public AnalysisResultNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
