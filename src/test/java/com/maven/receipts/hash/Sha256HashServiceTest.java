package com.maven.receipts.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class Sha256HashServiceTest {
    private HashService hashService;

    @BeforeEach
    void setup() {
        hashService = new Sha256HashService();
    }

    @Test
    void sameFileShouldProduceSameHash() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.txt",
                "text/plain",
                "hello".getBytes());

        String hash1 = hashService.sha256(file);
        String hash2 = hashService.sha256(file);

        assertEquals(hash1, hash2);
    }

    @Test
    void differentFilesShouldProduceDifferentHashes() {

        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "a.txt",
                "text/plain",
                "abc".getBytes());

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "b.txt",
                "text/plain",
                "xyz".getBytes());

        assertNotEquals(
                hashService.sha256(file1),
                hashService.sha256(file2));
    }

    @Test
    void hashShouldBe64Characters() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.txt",
                "text/plain",
                "sample".getBytes());

        assertEquals(
                64,
                hashService.sha256(file).length());
    }
}
