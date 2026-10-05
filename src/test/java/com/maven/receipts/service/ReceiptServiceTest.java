package com.maven.receipts.service;

import com.maven.receipts.config.StorageProperties;
import com.maven.receipts.dto.OcrResponse;
import com.maven.receipts.dto.ReceiptResponse;
import com.maven.receipts.dto.ReceiptUploadResponse;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.exception.InvalidReceiptFileException;
import com.maven.receipts.exception.ReceiptNotFoundException;
import com.maven.receipts.hash.HashService;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.impl.ReceiptServiceImpl;
import com.maven.receipts.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {
    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private HashService hashService;

    @Mock
    private StorageService storageService;

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private ReceiptServiceImpl service;

    private MockMultipartFile validFile;

    @BeforeEach
    void setup() {

        validFile = new MockMultipartFile(
                "file",
                "receipt.txt",
                "text/plain",
                "sample receipt".getBytes());

    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.txt",
                "text/plain",
                new byte[0]);

        assertThrows(
                InvalidReceiptFileException.class,
                () -> service.upload(file));
    }

    @Test
    void shouldRejectUnsupportedExtension() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.exe",
                "text/plain",
                "abc".getBytes());

        assertThrows(
                InvalidReceiptFileException.class,
                () -> service.upload(file));
    }

    @Test
    void shouldReturnExistingReceiptWhenDuplicateHashFound() {

        Receipt existing = Receipt.builder()
                .id(10L)
                .processed(true)
                .uploadedAt(Instant.now())
                .build();

        when(hashService.sha256(validFile))
                .thenReturn("hash123");

        when(receiptRepository.findByFileHash("hash123"))
                .thenReturn(Optional.of(existing));

        ReceiptUploadResponse response = service.upload(validFile);

        assertEquals(10L, response.receiptId());
        assertTrue(response.processed());

        verify(storageService, never())
                .store(any());
    }

    @Test
    void shouldUploadNewReceiptSuccessfully() {

        when(storageProperties.getUploadDirectory())
                .thenReturn("uploads");

        when(hashService.sha256(validFile))
                .thenReturn("hash123");

        when(receiptRepository.findByFileHash("hash123"))
                .thenReturn(Optional.empty());

        when(storageService.store(validFile))
                .thenReturn("stored-file.txt");

        Receipt saved = Receipt.builder()
                .id(1L)
                .processed(false)
                .uploadedAt(Instant.now())
                .storedFilename("stored-file.txt")
                .build();

        when(receiptRepository.saveAndFlush(any()))
                .thenReturn(saved);

        ReceiptUploadResponse response = service.upload(validFile);

        assertEquals(1L, response.receiptId());

        assertFalse(response.processed());

        verify(storageService)
                .store(validFile);

        verify(receiptRepository)
                .saveAndFlush(any(Receipt.class));
    }

    @Test
    void shouldHandleDuplicateInsertRaceCondition() {

        Receipt winner = Receipt.builder()
                .id(99L)
                .processed(false)
                .uploadedAt(Instant.now())
                .build();

        when(storageProperties.getUploadDirectory())
                .thenReturn("uploads");

        when(hashService.sha256(validFile))
                .thenReturn("hash123");

        when(receiptRepository.findByFileHash("hash123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));

        when(storageService.store(validFile))
                .thenReturn("stored-file.txt");

        when(receiptRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ReceiptUploadResponse response = service.upload(validFile);

        assertEquals(99L, response.receiptId());

        verify(storageService)
                .delete("stored-file.txt");
    }

    @Test
    void shouldThrowWhenReceiptNotFound() {

        when(receiptRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReceiptNotFoundException.class,
                () -> service.getReceipt(100L));
    }

    @Test
    void shouldReturnReceiptSuccessfully() {

        Receipt receipt = Receipt.builder()
                .id(1L)
                .originalFilename("receipt.txt")
                .uploadedAt(Instant.now())
                .processed(true)
                .build();

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.of(receipt));

        ReceiptResponse response = service.getReceipt(1L);

        assertEquals(1L, response.id());

        assertEquals(
                "receipt.txt",
                response.originalFilename());

        assertTrue(response.processed());
    }

    @Test
    void shouldThrowWhenOcrRequestedBeforeProcessing() {

        Receipt receipt = Receipt.builder()
                .processed(false)
                .build();

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.of(receipt));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getOcr(1L));

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode());
    }

    @Test
    void shouldReturnOcrSuccessfully() {

        Receipt receipt = Receipt.builder()
                .id(1L)
                .processed(true)
                .ocrText("OCR CONTENT")
                .build();

        when(receiptRepository.findById(1L))
                .thenReturn(Optional.of(receipt));

        OcrResponse response = service.getOcr(1L);

        assertEquals(1L, response.receiptId());

        assertEquals(
                "OCR CONTENT",
                response.ocrText());
    }

    @Test
    void shouldReturnReceiptWhenOriginalFilenameIsNull() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "text/plain",
                "abc".getBytes());

        when(hashService.sha256(file))
                .thenReturn("hash");

        when(receiptRepository.findByFileHash("hash"))
                .thenReturn(Optional.empty());

        when(storageService.store(file))
                .thenReturn("stored.txt");

        Receipt receipt = Receipt.builder()
                .id(5L)
                .processed(false)
                .uploadedAt(Instant.now())
                .build();

        when(receiptRepository.saveAndFlush(any()))
                .thenReturn(receipt);

        ReceiptUploadResponse response = service.upload(file);

        assertEquals(5L, response.receiptId());

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).saveAndFlush(receiptCaptor.capture());
        assertEquals("receipt.txt", receiptCaptor.getValue().getOriginalFilename());
    }

    @Test
    void shouldRejectFileLargerThan10Mb() {

        byte[] bytes = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.txt",
                "text/plain",
                bytes);

        assertThrows(
                InvalidReceiptFileException.class,
                () -> service.upload(file));
    }
}
