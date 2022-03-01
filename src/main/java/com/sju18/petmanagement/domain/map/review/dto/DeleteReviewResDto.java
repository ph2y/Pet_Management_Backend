package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class DeleteReviewResDto {
    private DtoMetadata _metadata;
    private Integer rating;

    // 정상 조회시 사용할 생성자
    public DeleteReviewResDto(DtoMetadata dtoMetadata, Integer rating) {
        this._metadata = dtoMetadata;
        this.rating = rating;
    }

    // 오류시 사용할 생성자
    public DeleteReviewResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.rating = null;
    }
}
