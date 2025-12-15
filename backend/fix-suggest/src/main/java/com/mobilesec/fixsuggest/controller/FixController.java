package com.mobilesec.fixsuggest.controller;

import com.mobilesec.fixsuggest.dto.FixRequest;
import com.mobilesec.fixsuggest.dto.FixResponse;
import com.mobilesec.fixsuggest.service.FixService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suggest")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend access
public class FixController {

    private final FixService fixService;

    @PostMapping
    public ResponseEntity<FixResponse> getSuggestion(@RequestBody FixRequest request) {
        FixResponse response = fixService.generateFix(request);
        return ResponseEntity.ok(response);
    }
}
