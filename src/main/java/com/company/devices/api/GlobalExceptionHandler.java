package com.company.devices.api;

import com.company.devices.api.dto.ErrorResponse;
import com.company.devices.domain.exception.DeviceNotFoundException;
import com.company.devices.domain.exception.InvalidDeviceOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DeviceNotFoundException ex) {
        return buildErrorResponse(NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidDeviceOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidDeviceOperationException ex) {
        return buildErrorResponse(BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailure(MethodArgumentNotValidException ex) {
        return buildErrorResponse(BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildErrorResponse(INTERNAL_SERVER_ERROR, "Unexpected server error:" + ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
