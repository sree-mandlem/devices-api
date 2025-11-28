package com.company.devices.domain.exception;

public class InvalidDeviceOperationException extends RuntimeException {
    public InvalidDeviceOperationException(String message) {
        super(message);
    }
}