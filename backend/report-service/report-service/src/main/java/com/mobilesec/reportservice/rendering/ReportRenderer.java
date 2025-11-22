package com.mobilesec.reportservice.rendering;

import com.mobilesec.reportservice.dto.AnalysisResultDto;

public interface ReportRenderer {

    ReportFormat format();

    RenderedReport render(AnalysisResultDto result);
}
