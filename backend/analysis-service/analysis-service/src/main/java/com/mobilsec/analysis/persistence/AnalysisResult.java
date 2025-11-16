package com.mobilsec.analysis.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "version_name")
    private String versionName;

    @Lob
    @Column(name = "permissions")
    private String permissionsJson;

    @Lob
    @Column(name = "manifest_flags")
    private String manifestFlagsJson;

    @Lob
    @Column(name = "exported_components")
    private String exportedComponentsJson;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AnalysisResult() {
    }

    public AnalysisResult(String packageName,
            String versionName,
            String permissionsJson,
            String manifestFlagsJson,
            String exportedComponentsJson,
            String riskLevel,
            Instant createdAt) {
        this.packageName = packageName;
        this.versionName = versionName;
        this.permissionsJson = permissionsJson;
        this.manifestFlagsJson = manifestFlagsJson;
        this.exportedComponentsJson = exportedComponentsJson;
        this.riskLevel = riskLevel;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPermissionsJson() {
        return permissionsJson;
    }

    public void setPermissionsJson(String permissionsJson) {
        this.permissionsJson = permissionsJson;
    }

    public String getManifestFlagsJson() {
        return manifestFlagsJson;
    }

    public void setManifestFlagsJson(String manifestFlagsJson) {
        this.manifestFlagsJson = manifestFlagsJson;
    }

    public String getExportedComponentsJson() {
        return exportedComponentsJson;
    }

    public void setExportedComponentsJson(String exportedComponentsJson) {
        this.exportedComponentsJson = exportedComponentsJson;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
