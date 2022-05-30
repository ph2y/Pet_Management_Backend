package com.sju18.petmanagement.domain.shop.product.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType {
    PRODUCT("product"), // 재화 (일반 상품)
    SERVICE("service"); // 서비스

    private final String ProductType;
}
