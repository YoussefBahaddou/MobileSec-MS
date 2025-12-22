package com.mobilesec.fixsuggest.service;

import com.mobilesec.fixsuggest.dto.FixRequest;
import com.mobilesec.fixsuggest.dto.FixResponse;

public interface FixService {
    FixResponse generateFix(FixRequest request);
}
