package com.mobilesec.reportservice.rendering;

import com.mobilesec.reportservice.dto.ComprehensiveReportDto;

public interface ReportRenderer {

    ReportFormat format();

    RenderedReport render(ComprehensiveReportDto result);
}
