package com.sju18.petmanagement.domain.community.block.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeleteBlockReqDto {
    @PositiveOrZero(message = "valid.account.id.notNegative")
    private Long id;
}
