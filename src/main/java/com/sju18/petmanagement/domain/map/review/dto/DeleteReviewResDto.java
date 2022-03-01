package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class DeleteReviewResDto {
    private DtoMetadata _metadata;
    private Integer deletedReviewRating;

    // 정상 조회시 사용할 생성자
    public DeleteReviewResDto(DtoMetadata dtoMetadata, Integer deletedReviewRating) {
        this._metadata = dtoMetadata;
        this.deletedReviewRating = deletedReviewRating;
    }

    // 오류시 사용할 생성자
    public DeleteReviewResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.deletedReviewRating = null;
    }
}
