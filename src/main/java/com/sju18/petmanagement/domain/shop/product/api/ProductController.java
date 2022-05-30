package com.sju18.petmanagement.domain.shop.product.api;

import com.sju18.petmanagement.domain.shop.product.application.ProductService;
import com.sju18.petmanagement.domain.shop.product.dao.Product;
import com.sju18.petmanagement.domain.shop.product.dao.ProductType;
import com.sju18.petmanagement.domain.shop.product.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class ProductController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getShopMessageSource();
    private final ProductService productServ;

    // CREATE
    @PostMapping("/api/shop/product")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductReqDto reqDto) {
        DtoMetadata dtoMetadata;
        Long productId;

        try {
            productId = productServ.createProduct(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateProductResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.product.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateProductResDto(dtoMetadata, productId));
    }

    // READ
    @GetMapping("/api/shop/product")
    public ResponseEntity<?> fetchProduct(@Valid FetchProductReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Product> productList;
        Pageable pageable = null;
        Boolean isLast = null;

        try {
            if (reqDto.getId() != null) {
                // 개별 상품 조회 요청
                productList = new ArrayList<>();
                productList.add(productServ.fetchProductById(reqDto.getId()));
            } else if (reqDto.getType() != null) {
                // 해당 상점의 재화 또는 서비스 목록 조회 요청
                final Page<Product> productPage = productServ.fetchProductByPlaceAndType(reqDto.getPlaceId(), ProductType.valueOf(reqDto.getType()), reqDto.getPageIndex());
                productList = productPage.getContent();
                pageable = productPage.getPageable();
                isLast = productPage.isLast();
            } else if (reqDto.getPlaceId() != null) {
                // 해당 상점의 전체 판매목록 조회 요청
                final Page<Product> productPage = productServ.fetchProductByPlace(reqDto.getPlaceId(), reqDto.getPageIndex());
                productList = productPage.getContent();
                pageable = productPage.getPageable();
                isLast = productPage.isLast();
            } else {
                throw new Exception(msgSrc.getMessage("res.product.fetch.invalidQuery", null, Locale.ENGLISH));
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(FetchProductResDto.builder()._metadata(dtoMetadata).build());
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.product.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(FetchProductResDto.builder()
                ._metadata(dtoMetadata)
                .productList(productList)
                .pageable(pageable)
                .isLast(isLast)
                .build()
        );

    }

    // UPDATE
    @PutMapping("/api/shop/product")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody UpdateProductReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            productServ.updateProduct(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateProductResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.product.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateProductResDto(dtoMetadata));
    }

    // DELETE
    @DeleteMapping("/api/shop/product")
    public ResponseEntity<?> deleteProduct(@Valid @RequestBody DeleteProductReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            productServ.deleteProduct(reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteProductResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.product.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteProductResDto(dtoMetadata));
    }
}
