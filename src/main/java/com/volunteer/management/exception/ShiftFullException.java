package com.volunteer.management.exception;

public class ShiftFullException extends RuntimeException {
    public ShiftFullException(String message) {
        super(message);
    }
}