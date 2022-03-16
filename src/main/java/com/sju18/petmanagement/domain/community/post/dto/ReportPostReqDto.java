package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class ReportPostReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
}
