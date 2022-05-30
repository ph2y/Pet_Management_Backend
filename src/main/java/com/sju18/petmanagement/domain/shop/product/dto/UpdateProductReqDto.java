package com.sju18.petmanagement.domain.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class UpdateProductReqDto {
    @PositiveOrZero(message = "valid.product.id.notNegative")
    private Long id;
    @NotBlank(message = "valid.product.name.blank")
    @Size(max = 20, message = "valid.product.name.size")
    private String name;
    @NotBlank(message = "valid.product.type.price")
    private Long price;
    @NotBlank(message = "valid.product.type.prepaid")
    private Boolean prepaid;
    @NotBlank(message = "valid.product.type.stock")
    private Long stock;
    private String status;
    @NotBlank(message = "valid.product.type.visible")
    private Boolean visible;
    @Size(max = 5000, message = "valid.product.description.size")
    private String description;
    private String shippingType;
    private Long shippingPrice;
    private Long reservationPrice;
    private String returnPolicy;
}
