package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
public class FetchPostReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long petId;
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
    @DecimalMax(value = "90.0", message = "valid.position.latitude.max")
    @DecimalMin(value = "-90.0", message = "valid.position.latitude.min")
    private Double currentLat;
    @DecimalMax(value = "180.0", message = "valid.position.longitude.max")
    @DecimalMin(value = "-180.0", message = "valid.position.longitude.min")
    private Double currentLong;
    @PositiveOrZero(message = "valid.post.pageIndex.notNegative")
    private Integer pageIndex;
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long topPostId;
}
