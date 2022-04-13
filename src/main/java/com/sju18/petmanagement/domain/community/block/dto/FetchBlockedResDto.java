package com.sju18.petmanagement.domain.community.block.dto;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

import java.util.List;

@Data
public class FetchBlockedResDto {
    private DtoMetadata _metadata;
    private List<Account> blockedList;

    public FetchBlockedResDto(DtoMetadata metadata, List<Account> blockedList) {
        this._metadata = metadata;
        this.blockedList = blockedList;
    }

    // 오류시 사용할 생성자
    public FetchBlockedResDto(DtoMetadata metadata) {
        this._metadata = metadata;
        this.blockedList = null;
    }
}
