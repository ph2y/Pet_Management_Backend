package com.sju18.petmanagement.domain.shop.product.application;

import com.sju18.petmanagement.domain.map.place.application.PlaceService;
import com.sju18.petmanagement.domain.shop.product.dao.*;
import com.sju18.petmanagement.domain.shop.product.dto.CreateProductReqDto;
import com.sju18.petmanagement.domain.shop.product.dto.DeleteProductReqDto;
import com.sju18.petmanagement.domain.shop.product.dto.UpdateProductReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final MessageSource msgSrc = MessageConfig.getShopMessageSource();
    private final ProductRepository productRepository;
    private final PlaceService placeServ;

    // CREATE
    @Transactional
    public Long createProduct(CreateProductReqDto reqDto) throws Exception {
        Product product = Product.builder()
                .name(reqDto.getName())
                .place(placeServ.fetchPlaceById(reqDto.getPlaceId()))
                .type(ProductType.valueOf(reqDto.getType()).getProductType())
                .price(reqDto.getPrice())
                .prepaid(reqDto.getPrepaid())
                .stock(reqDto.getStock())
                .timestamp(LocalDateTime.now())
                .status(reqDto.getStatus() == null ? ProductStatus.DISABLED.getProductStatus() : reqDto.getStatus())
                .visible(reqDto.getVisible())
                .description(reqDto.getDescription())
                .returnPolicy(reqDto.getReturnPolicy())
                .build();

        switch (ProductType.valueOf(reqDto.getType())) {
            case PRODUCT:
                product.setShippingType(reqDto.getShippingType() == null ? ProductShippingType.SEND_QUERY.getProductShippingType() : product.getShippingType());
                product.setShippingPrice(reqDto.getShippingPrice() == null ? 0 : reqDto.getShippingPrice());
                productRepository.save(product);
                return product.getId();
            case SERVICE:
                product.setReservationPrice(reqDto.getReservationPrice() == null ? 0 : reqDto.getReservationPrice());
                productRepository.save(product);
                return product.getId();
            default:
                throw new Exception(msgSrc.getMessage("error.product.type.notExists", null, Locale.ENGLISH));
        }
    }

    // READ
    @Transactional(readOnly = true)
    public Product fetchProductById(Long productId) throws Exception {
        return productRepository.findById(productId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.product.notExists", null, Locale.ENGLISH)
                ));
    }
    @Transactional(readOnly = true)
    public Page<Product> fetchProductByPlace(Long placeId, Integer pageIndex) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 50, Sort.Direction.DESC, "product_id");
        return productRepository.findAllByPlaceId(placeId, pageQuery);
    }
    @Transactional(readOnly = true)
    public Page<Product> fetchProductByPlaceAndType(Long placeId, ProductType type, Integer pageIndex) {
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 50, Sort.Direction.DESC, "product_id");
        return productRepository.findAllByPlaceIdAndTypeContains(placeId, type.getProductType(), pageQuery);
    }

    // UPDATE
    @Transactional
    public void updateProduct(UpdateProductReqDto reqDto) throws Exception {
        Product currentProduct = this.fetchProductById(reqDto.getId());

        if (!reqDto.getName().equals(currentProduct.getName())) {
            currentProduct.setName(reqDto.getName());
        }
        if (!reqDto.getPrice().equals(currentProduct.getPrice())) {
            currentProduct.setPrice(reqDto.getPrice());
        }
        if (!reqDto.getPrepaid().equals(currentProduct.getPrepaid())) {
            currentProduct.setPrepaid(reqDto.getPrepaid());
        }
        if (!reqDto.getStock().equals(currentProduct.getStock())) {
            currentProduct.setStock(reqDto.getStock());
        }
        if (reqDto.getStatus() != null) {
            if (!reqDto.getStatus().equals(currentProduct.getStatus())) {
                currentProduct.setStatus(reqDto.getStatus());
            }
        }
        if (!reqDto.getVisible().equals(currentProduct.getVisible())) {
            currentProduct.setVisible(reqDto.getVisible());
        }
        if (!reqDto.getDescription().equals(currentProduct.getDescription())) {
            currentProduct.setDescription(reqDto.getDescription());
        }
        if (!reqDto.getReturnPolicy().equals(currentProduct.getReturnPolicy())) {
            currentProduct.setReturnPolicy(reqDto.getReturnPolicy());
        }
        switch (ProductType.valueOf(currentProduct.getType())) {
            case PRODUCT:
                if (reqDto.getShippingType() != null) {
                    currentProduct.setShippingType(reqDto.getShippingType());
                }
                if (reqDto.getShippingPrice() != null) {
                    currentProduct.setShippingPrice(reqDto.getShippingPrice());
                }
                productRepository.save(currentProduct);
            case SERVICE:
                if (reqDto.getReservationPrice() != null) {
                    currentProduct.setReservationPrice(reqDto.getReservationPrice());
                }
                productRepository.save(currentProduct);
            default:
                throw new Exception(msgSrc.getMessage("error.product.type.notExists", null, Locale.ENGLISH));
        }
    }

    // DELETE
    @Transactional
    public void deleteProduct(DeleteProductReqDto reqDto) throws Exception {
        Product product = this.fetchProductById(reqDto.getId());
        productRepository.delete(product);
    }
}
