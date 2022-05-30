package com.sju18.petmanagement.domain.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchProductReqDto {
    @PositiveOrZero(message = "valid.product.id.notNegative")
    private Long id;
    @PositiveOrZero(message = "valid.product.placeId.notNegative")
    private Long placeId;
    private String type;
    @PositiveOrZero(message = "valid.product.pageIndex.notNegative")
    private Integer pageIndex;
}
