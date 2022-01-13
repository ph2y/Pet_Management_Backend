package com.sju18.petmanagement.domain.community.like.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FetchLikeResDto {
    private DtoMetadata _metadata;
    private Long likedCount;
    private List<Long> likedAccountIdList;
}
