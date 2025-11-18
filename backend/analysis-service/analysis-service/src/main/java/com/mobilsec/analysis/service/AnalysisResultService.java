package com.mobilsec.analysis.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mobilsec.analysis.persistence.AnalysisResult;
import com.mobilsec.analysis.persistence.AnalysisResultRepository;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;

@Service
public class AnalysisResultService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AnalysisResultRepository repository;
    private final AnalysisResultMapper mapper;

    public AnalysisResultService(AnalysisResultRepository repository, AnalysisResultMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<AnalysisResultDto> list(int page, int size) {
        validatePaging(page, size);
        Pageable pageable = PageRequest.of(page, size, DEFAULT_SORT);
        Page<AnalysisResult> results = repository.findAll(pageable);

        List<AnalysisResultDto> summaries = results.getContent().stream()
                .map(mapper::fromEntity)
                .toList();

        return new PageImpl<>(summaries, pageable, results.getTotalElements());
    }

    public AnalysisResultDto getById(Long id) {
        AnalysisResult result = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis result not found"));
        return mapper.fromEntity(result);
    }

    public AnalysisResultDto getLatestByPackage(String packageName) {
        AnalysisResult result = repository.findTopByPackageNameOrderByCreatedAtDesc(packageName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not analysed"));
        return mapper.fromEntity(result);
    }

    private void validatePaging(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters");
        }
    }

}
