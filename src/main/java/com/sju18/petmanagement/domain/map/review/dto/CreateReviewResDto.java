package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CreateReviewResDto {
    private DtoMetadata _metadata;
    private Long id;

    // 정상 조회시 사용할 생성자
    public CreateReviewResDto(DtoMetadata dtoMetadata, Long reviewId) {
        this._metadata = dtoMetadata;
        this.id = reviewId;
    }

    // 오류시 사용할 생성자
    public CreateReviewResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
    }
}
