package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;   // SUCCESS / FAILURE
    private T data;          // ID, DTO, List, etc.
    private String message;  // success/failure reason
}


