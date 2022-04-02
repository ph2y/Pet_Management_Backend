package com.sju18.petmanagement.domain.map.place.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findById(Long id);
    @Query(
            value = "SELECT * FROM place AS p WHERE (:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)",
            nativeQuery = true
    )
    List<Place> findAllByDistance(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax
    );
    @Query(
            value = "SELECT * FROM place AS p WHERE ((:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)) AND (p.category_code = :categoryCode)",
            nativeQuery = true
    )
    List<Place> findAllByCategoryCodeAndDistance(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("categoryCode") String categoryCode
    );
    @Query(
            value = "SELECT * FROM place AS p WHERE ((:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)) AND (p.name LIKE %:keyword%)",
            nativeQuery = true
    )
    List<Place> findAllByKeywordAndDistance(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("keyword") String keyword
    );
    @Query(
            value = "SELECT * FROM place AS p WHERE (:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax) AND RAND() <= :sampleSize",
            nativeQuery = true
    )
    List<Place> findAllByDistanceWithRandomSampling(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("sampleSize") Double sampleSize
    );
    @Query(
            value = "SELECT * FROM place AS p WHERE ((:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)) AND (p.category_code = :categoryCode)  AND RAND() <= :sampleSize",
            nativeQuery = true
    )
    List<Place> findAllByCategoryCodeAndDistanceWithRandomSampling(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("categoryCode") String categoryCode,
            @Param("sampleSize") Double sampleSize
    );
    @Query(
            value = "SELECT * FROM place AS p WHERE ((:latMin <= p.latitude AND p.latitude <= :latMax) AND (:longMin <= p.longitude AND p.longitude <= :longMax)) AND (p.name LIKE %:keyword%) AND RAND() <= :sampleSize",
            nativeQuery = true
    )
    List<Place> findAllByKeywordAndDistanceWithRandomSampling(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("keyword") String keyword,
            @Param("sampleSize") Double sampleSize
    );
}