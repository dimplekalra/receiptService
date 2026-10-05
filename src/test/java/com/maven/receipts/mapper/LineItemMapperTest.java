package com.maven.receipts.mapper;

import com.maven.receipts.dto.UpdateItemsRequest;
import com.maven.receipts.entity.LineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LineItemMapperTest {
    private final LineItemMapper mapper = new LineItemMapper();

    @Test
    void shouldMapRequestToEntity() {

        UpdateItemsRequest.ItemRequest request = new UpdateItemsRequest.ItemRequest(
                "Coffee",
                new BigDecimal("5.50"),
                new BigDecimal("2"),
                new BigDecimal("0.50"));

        LineItem item = mapper.toEntity(request);

        assertEquals("Coffee", item.getDescription());

        assertEquals(
                new BigDecimal("5.50"),
                item.getAmount());

        assertEquals(
                new BigDecimal("2"),
                item.getQuantity());

        assertEquals(
                new BigDecimal("0.50"),
                item.getTaxAmount());
    }
}
