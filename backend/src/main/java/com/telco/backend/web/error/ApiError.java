package com.telco.backend.web.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
public class ApiError {

    private Instant timestamp;
    private String path;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
}