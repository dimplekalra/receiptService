package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UpdateItemsRequest;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.exception.MoneyMismatchException;
import com.maven.receipts.exception.TransactionNotFoundException;
import com.maven.receipts.mapper.LineItemMapper;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.service.impl.TransactionUpdateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class TransactionUpdateServiceTest {
    @Mock
    ExpenseTransactionRepository repository;

    @Mock
    LineItemMapper mapper;

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    MoneyReconciliationService reconciliation;

    @InjectMocks
    TransactionUpdateServiceImpl service;

    @Test
    void shouldThrowWhenTransactionMissing() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> service.updateItems(
                        1L,
                        new UpdateItemsRequest(List.of())));
    }

    @Test
    void shouldThrowMoneyMismatch() {

        ExpenseTransaction tx = ExpenseTransaction.builder().build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(tx));

        UpdateItemsRequest.ItemRequest req = new UpdateItemsRequest.ItemRequest(
                "Coffee",
                BigDecimal.ONE,
                null,
                null);

        LineItem item = LineItem.builder()
                .amount(BigDecimal.ONE)
                .build();

        when(mapper.toEntity(any()))
                .thenReturn(item);

        when(reconciliation.reconcile(eq(tx), any()))
                .thenReturn(
                        new MoneyReconciliationService.ReconciliationResult(
                                false,
                                BigDecimal.TEN,
                                BigDecimal.ONE,
                                BigDecimal.TEN));

        assertThrows(
                MoneyMismatchException.class,
                () -> service.updateItems(
                        1L,
                        new UpdateItemsRequest(List.of(req))));
    }

    @Test
    void shouldUpdateSuccessfully() {

        ExpenseTransaction tx = ExpenseTransaction.builder().build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(tx));

        UpdateItemsRequest.ItemRequest req = new UpdateItemsRequest.ItemRequest(
                "Coffee",
                BigDecimal.ONE,
                null,
                null);

        LineItem item = LineItem.builder()
                .amount(BigDecimal.ONE)
                .build();

        when(mapper.toEntity(any()))
                .thenReturn(item);

        when(reconciliation.reconcile(eq(tx), any()))
                .thenReturn(
                        new MoneyReconciliationService.ReconciliationResult(
                                true,
                                BigDecimal.ONE,
                                BigDecimal.ONE,
                                BigDecimal.ZERO));

        when(repository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = mock(TransactionResponse.class);

        when(transactionMapper.toResponse(any()))
                .thenReturn(response);

        assertEquals(
                response,
                service.updateItems(
                        1L,
                        new UpdateItemsRequest(List.of(req))));
    }
}
