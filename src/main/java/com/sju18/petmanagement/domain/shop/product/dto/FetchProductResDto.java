package com.sju18.petmanagement.domain.shop.product.dto;

import com.sju18.petmanagement.domain.shop.product.dao.Product;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Data
@Builder
public class FetchProductResDto {
    private DtoMetadata _metadata;
    private List<Product> productList;
    private Pageable pageable;
    private Boolean isLast;
}
