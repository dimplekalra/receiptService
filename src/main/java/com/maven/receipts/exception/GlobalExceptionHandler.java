package com.maven.receipts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.maven.receipts.dto.MoneyMismatchResponse;

import java.time.Instant;
import java.util.Map;

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

        @ExceptionHandler(TransactionNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleTransactionNotFound(
                        TransactionNotFoundException ex) {

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(
                                                Instant.now(),
                                                404,
                                                "TRANSACTION_NOT_FOUND",
                                                ex.getMessage()

                                ));

        }

        public record ErrorResponse(
                        Instant timestamp,
                        int status,
                        String error,
                        String message) {
        }

        @ExceptionHandler(MoneyMismatchException.class)
        public ResponseEntity<MoneyMismatchResponse> handleMoneyMismatch(
                        MoneyMismatchException ex) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(
                                                new MoneyMismatchResponse(
                                                                "ITEM_TOTAL_MISMATCH",
                                                                ex.getExpected(),
                                                                ex.getActual(),
                                                                ex.getDifference()));
        }

        @ExceptionHandler(ProcessedReceiptDeletionException.class)
        public ResponseEntity<?> handleProcessedReceiptDeletion(
                        ProcessedReceiptDeletionException ex) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error",
                                                ex.getMessage()));
        }
}
