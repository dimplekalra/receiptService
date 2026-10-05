package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.exception.ReceiptNotFoundException;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.ocr.OcrService;
import com.maven.receipts.processing.ParsedReceipt;
import com.maven.receipts.processing.ReceiptParser;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.impl.ReceiptProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class ReceiptProcessingServiceTest {

    @Mock
    MoneyReconciliationService reconciliationService;

    @Mock
    ReceiptRepository receiptRepository;

    @Mock
    ExpenseTransactionRepository transactionRepository;

    @Mock
    OcrService ocrService;

    @Mock
    ReceiptParser parser;

    @Mock
    TransactionMapper mapper;

    @InjectMocks
    ReceiptProcessingServiceImpl service;

    Receipt receipt;

    @BeforeEach
    void setup() {

        receipt = Receipt.builder()
                .id(1L)
                .build();
    }

    @Test
    void shouldThrowWhenReceiptMissing() {

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReceiptNotFoundException.class,
                () -> service.process(1L));

    }

    @Test
    void shouldProcessReceiptSuccessfully() {

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.of(receipt));

        when(ocrService.extractText(receipt))
                .thenReturn("OCR");

        ParsedReceipt parsed = new ParsedReceipt(
                "Cafe",
                LocalDate.now(),
                "EUR",
                new BigDecimal("10.00"),
                List.of(),
                List.of(
                        new ParsedReceipt.ParsedLineItem(
                                "Coffee",
                                new BigDecimal("10.00"))));

        when(parser.parse("OCR"))
                .thenReturn(parsed);

        ExpenseTransaction tx = ExpenseTransaction.builder()
                .receipt(receipt)
                .build();

        when(transactionRepository.findByReceiptId(1L))
                .thenReturn(Optional.of(tx));

        when(reconciliationService.reconcile(any(ExpenseTransaction.class), anyList()))
                .thenReturn(new MoneyReconciliationService.ReconciliationResult(
                        true,
                        new BigDecimal("10.00"),
                        new BigDecimal("10.00"),
                        BigDecimal.ZERO));

        when(transactionRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = mock(TransactionResponse.class);

        when(mapper.toResponse(any()))
                .thenReturn(response);

        TransactionResponse actual = service.process(1L);

        assertEquals(response, actual);

        verify(ocrService).extractText(receipt);

        verify(parser).parse("OCR");

        verify(transactionRepository).save(any());

        verify(transactionRepository).flush();
    }

    @Test
    void shouldCreateTransactionIfMissing() {

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.of(receipt));

        when(ocrService.extractText(any()))
                .thenReturn("OCR");

        when(parser.parse(any()))
                .thenReturn(new ParsedReceipt(
                        "Cafe",
                        LocalDate.now(),
                        "EUR",
                        new BigDecimal("10"),
                        List.of(),
                        List.of()));

        when(transactionRepository.findByReceiptId(1L))
                .thenReturn(Optional.empty());

        when(reconciliationService.reconcile(any(ExpenseTransaction.class), anyList()))
                .thenReturn(new MoneyReconciliationService.ReconciliationResult(
                        false,
                        new BigDecimal("10.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("10.00")));

        when(transactionRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(mapper.toResponse(any()))
                .thenReturn(mock(TransactionResponse.class));

        service.process(1L);

        verify(transactionRepository).save(any());
    }
}
