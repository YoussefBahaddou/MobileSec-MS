package com.mobilsec.analysis.service;

import java.util.List;

public record ApkMetadata(
        String packageName,
        String versionName,
        List<String> permissions,
        ManifestFlags manifestFlags,
        List<ExportedComponent> exportedComponents) {
}
