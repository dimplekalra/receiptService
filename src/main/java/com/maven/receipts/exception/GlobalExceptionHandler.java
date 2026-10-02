package com.maven.receipts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidReceiptFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(
            InvalidReceiptFileException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        Instant.now(),
                        400,
                        "INVALID_RECEIPT_FILE",
                        exception.getMessage()));
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ReceiptNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        Instant.now(),
                        404,
                        "RECEIPT_NOT_FOUND",
                        exception.getMessage()));
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message) {
    }
}
