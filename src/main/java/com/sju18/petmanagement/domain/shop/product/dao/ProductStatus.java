package com.sju18.petmanagement.domain.shop.product.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    ON_SALE("on-sale"), // 판매중
    NO_STOCK("no-stock"),   // 재고없음
    SEND_QUERY("send-query"),   // 별도문의
    DISABLED("disabled");   // 판매중지

    private final String productStatus;
}
