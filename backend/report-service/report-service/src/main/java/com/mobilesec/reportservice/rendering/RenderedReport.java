package com.mobilesec.reportservice.rendering;

public record RenderedReport(byte[] content, ReportFormat format, String filename) {

    public String contentType() {
        return format.getMediaType();
    }

    public boolean isBinary() {
        return format.isBinary();
    }
}
