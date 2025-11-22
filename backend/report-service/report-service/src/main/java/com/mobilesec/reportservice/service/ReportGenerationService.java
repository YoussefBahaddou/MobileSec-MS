package com.mobilesec.reportservice.service;

import com.mobilesec.reportservice.client.AnalysisServiceClient;
import com.mobilesec.reportservice.dto.AnalysisResultDto;
import com.mobilesec.reportservice.exception.AnalysisResultNotFoundException;
import com.mobilesec.reportservice.rendering.RenderedReport;
import com.mobilesec.reportservice.rendering.ReportFormat;
import com.mobilesec.reportservice.rendering.ReportRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportGenerationService {

    private final AnalysisServiceClient analysisServiceClient;
    private final Map<ReportFormat, ReportRenderer> renderersByFormat;

    public ReportGenerationService(AnalysisServiceClient analysisServiceClient, List<ReportRenderer> renderers) {
        this.analysisServiceClient = analysisServiceClient;
        this.renderersByFormat = new EnumMap<>(ReportFormat.class);
        for (ReportRenderer renderer : renderers) {
            renderersByFormat.put(renderer.format(), renderer);
        }
    }

    public RenderedReport generateById(long id, ReportFormat format) {
        AnalysisResultDto result = fetchAnalysis(() -> analysisServiceClient.fetchById(id),
                "No analysis result found for id " + id);
        return render(format, result);
    }

    public RenderedReport generateByPackage(String packageName, ReportFormat format) {
        AnalysisResultDto result = fetchAnalysis(() -> analysisServiceClient.fetchByPackage(packageName),
                "No analysis result found for package " + packageName);
        return render(format, result);
    }

    private RenderedReport render(ReportFormat format, AnalysisResultDto result) {
        ReportRenderer renderer = renderersByFormat.get(format);
        if (renderer == null) {
            throw new IllegalArgumentException("Unsupported report format: " + format);
        }
        return renderer.render(result);
    }

    private AnalysisResultDto fetchAnalysis(AnalysisSupplier supplier, String notFoundMessage) {
        try {
            AnalysisResultDto result = supplier.get().block();
            if (result == null) {
                throw new AnalysisResultNotFoundException(notFoundMessage);
            }
            return result;
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AnalysisResultNotFoundException(notFoundMessage, ex);
            }
            throw new IllegalStateException("Call to analysis-service failed: " + ex.getStatusCode(), ex);
        }
    }

    @FunctionalInterface
    private interface AnalysisSupplier {
        reactor.core.publisher.Mono<AnalysisResultDto> get();
    }
}
