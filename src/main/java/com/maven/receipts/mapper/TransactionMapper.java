package com.maven.receipts.mapper;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionResponse toResponse(
            ExpenseTransaction transaction) {

        return new TransactionResponse(

                transaction.getId(),

                transaction.getReceipt().getId(),

                transaction.getMerchant(),

                transaction.getTransactionDate(),

                transaction.getCurrency(),

                transaction.getGrandTotal(),

                transaction.getItemizeStatus(),

                transaction.getTaxes()
                        .stream()
                        .map(tax -> new TransactionResponse.TaxResponse(

                                tax.getId(),

                                tax.getName(),

                                tax.getRate(),

                                tax.getAmount()

                        ))
                        .toList(),

                transaction.getLineItems()
                        .stream()
                        .map(item -> new TransactionResponse.LineItemResponse(

                                item.getId(),

                                item.getDescription(),

                                item.getAmount(),

                                item.getQuantity(),

                                item.getTaxAmount()

                        ))
                        .toList()

        );

    }
}
