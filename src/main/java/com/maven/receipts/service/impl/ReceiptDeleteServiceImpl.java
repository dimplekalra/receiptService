package com.maven.receipts.service.impl;

import com.maven.receipts.entity.Receipt;
import com.maven.receipts.exception.ProcessedReceiptDeletionException;
import com.maven.receipts.exception.ReceiptNotFoundException;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.ReceiptDeleteService;
import com.maven.receipts.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptDeleteServiceImpl implements ReceiptDeleteService {
    private final ReceiptRepository receiptRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public void delete(Long receiptId) {

        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ReceiptNotFoundException(receiptId));

        /*
         * Assignment rule:
         *
         * Never delete receipts that already
         * produced money.
         */
        if (receipt.getTransaction() != null) {
            throw new ProcessedReceiptDeletionException(receiptId);
        }

        storageService.delete(
                receipt.getStoredFilename());

        receiptRepository.delete(receipt);
    }
}
