package com.maven.receipts.mapper;

import org.springframework.stereotype.Component;

import com.maven.receipts.dto.UpdateItemsRequest;
import com.maven.receipts.entity.LineItem;

@Component
public class LineItemMapper {
    public LineItem toEntity(
            UpdateItemsRequest.ItemRequest request) {

        return LineItem.builder()
                .description(request.description())
                .amount(request.amount())
                .quantity(request.quantity())
                .taxAmount(request.taxAmount())
                .build();

    }
}
