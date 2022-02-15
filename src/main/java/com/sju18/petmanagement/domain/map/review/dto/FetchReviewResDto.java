package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.domain.map.review.dao.Review;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class FetchReviewResDto {
    private DtoMetadata _metadata;
    private List<Review> reviewList;
    private Pageable pageable;
    private Boolean isLast;

    // 정상 조회시 사용할 생성자
    public FetchReviewResDto(DtoMetadata dtoMetadata, List<Review> reviewList, Pageable pageable, Boolean isLast) {
        this._metadata = dtoMetadata;
        this.reviewList = reviewList;
        this.pageable = pageable;
        this.isLast = isLast;
    }

    // 오류시 사용할 생성자
    public FetchReviewResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.reviewList = null;
        this.pageable = null;
        this.isLast = null;
    }
}
