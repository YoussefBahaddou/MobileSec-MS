package com.mobilesec.reportservice.controller;

import com.mobilesec.reportservice.exception.AnalysisResultNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AdviceController {

        @ExceptionHandler(AnalysisResultNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(AnalysisResultNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                                "error", "ANALYSIS_NOT_FOUND",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
                return ResponseEntity.badRequest()
                                .body(Map.of(
                                                "error", "INVALID_REQUEST",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                .body(Map.of(
                                                "error", "ANALYSIS_SERVICE_UNAVAILABLE",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleJsonError(
                        org.springframework.http.converter.HttpMessageNotReadableException ex) {
                ex.printStackTrace();
                return ResponseEntity.badRequest()
                                .body(Map.of(
                                                "error", "JSON_PARSE_ERROR",
                                                "message", ex.getMessage()));
        }
}
