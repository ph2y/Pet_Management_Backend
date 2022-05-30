package com.sju18.petmanagement.domain.shop.product.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateProductResDto {
    private DtoMetadata _metadata;
    private Long productId;
}
