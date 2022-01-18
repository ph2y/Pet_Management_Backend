package com.sju18.petmanagement.domain.account.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class FetchFcmRegistrationTokenResDto {
    private DtoMetadata _metadata;
    private String fcmRegistrationToken;

    // 정상 조회시 사용할 생성자
    public FetchFcmRegistrationTokenResDto(DtoMetadata dtoMetadata, String fcmRegistrationToken) {
        this._metadata = dtoMetadata;
        this.fcmRegistrationToken = fcmRegistrationToken;
    }

    // 오류시 사용할 생성자
    public FetchFcmRegistrationTokenResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.fcmRegistrationToken = null;
    }
}
