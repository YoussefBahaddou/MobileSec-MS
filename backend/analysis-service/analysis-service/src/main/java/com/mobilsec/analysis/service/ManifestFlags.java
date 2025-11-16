package com.mobilsec.analysis.service;

public record ManifestFlags(
        boolean debuggable,
        boolean allowBackup,
        boolean cleartextTrafficPermitted) {
}
