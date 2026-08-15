package com.telco.backend.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ApiError {

    private Instant timestamp;
    private String path;
    private String error;
    private String message;
}