package com.akentech.shared.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResult {
    private int recordsProcessed;
    private String message;
}