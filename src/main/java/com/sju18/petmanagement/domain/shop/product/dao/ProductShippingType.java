package com.sju18.petmanagement.domain.shop.product.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductShippingType {
    PICK_UP("pick-up"), // 방문수령
    PARCEL("parcel"),   // 택배배송
    DIGITAL_DOWNLOAD("digital-download"),   // 디지털 다운로드(전자상품)
    SEND_QUERY("send-query"),   // 별도문의
    DISABLED("disabled");   // 해당사항없음 (재화가 아닌 경우)

    private final String productShippingType;
}
