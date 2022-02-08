package com.sju18.petmanagement.domain.map.place.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryCode {
    SHOP("shop"), // 매장
    HOSPITAL("hospital"), // 병원
    HOTEL("hotel"), // 호텔
    CAFE("cafe"), // 카페
    SALON("salon"), // 미용실
    PARK("park"), // 공원
    SHELTER("shelter"); // 보호소

    private final String categoryCode;
}
