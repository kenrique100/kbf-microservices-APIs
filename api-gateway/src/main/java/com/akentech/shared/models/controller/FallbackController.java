package com.akentech.shared.models.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String fallback() {
        return "{\"message\": \"Service is currently unavailable. Please try again later.\"}";
    }
}