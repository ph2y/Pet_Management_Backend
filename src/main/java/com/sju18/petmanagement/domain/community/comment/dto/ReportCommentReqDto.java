package com.sju18.petmanagement.domain.community.comment.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class ReportCommentReqDto {
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long id;
}
