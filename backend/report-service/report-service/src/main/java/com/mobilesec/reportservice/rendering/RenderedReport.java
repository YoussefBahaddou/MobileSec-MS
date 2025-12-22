package com.mobilesec.reportservice.rendering;

public record RenderedReport(byte[] content, String filename, String contentType) {
}
