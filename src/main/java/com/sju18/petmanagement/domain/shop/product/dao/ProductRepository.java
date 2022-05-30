package com.sju18.petmanagement.domain.shop.product.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByPlaceId(Long placeId, Pageable pageable);
    Page<Product> findAllByPlaceIdAndTypeContains(Long placeId, String type, Pageable pageable);
}
