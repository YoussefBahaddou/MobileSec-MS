package com.mobilesec.reportgen.rendering;

import com.mobilesec.reportgen.dto.ComprehensiveReportDto;

public interface ReportRenderer {
    ReportFormat format();

    RenderedReport render(ComprehensiveReportDto data);
}
