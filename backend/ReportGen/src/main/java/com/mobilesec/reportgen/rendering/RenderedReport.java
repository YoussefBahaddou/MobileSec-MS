package com.mobilesec.reportgen.rendering;

public record RenderedReport(byte[] content, String filename, String contentType) {
}
