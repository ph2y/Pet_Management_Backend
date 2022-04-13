package com.sju18.petmanagement.domain.map.review.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Review> findByAuthorAndId(Account author, Long id);

    Optional<Review> findByPlaceIdAndAuthorId(Long placeId, Long authorId);

    @Query(
            value = "SELECT * FROM review AS r WHERE r.place_id=:placeId AND r.account_id NOT IN :blocked",
            countQuery = "SELECT COUNT(*) FROM review AS r WHERE r.place_id=:placeId AND r.account_id NOT IN :blocked",
            nativeQuery = true
    )
    Page<Review> findAllByPlaceId(
            @Param("placeId") Long placeId,
            @Param("blocked") Collection<Long> blockedAccountIdList,
            Pageable pageable);

    @Query(
            value = "SELECT * FROM review AS r WHERE r.review_id <= :top AND r.place_id=:placeId AND r.account_id NOT IN :blocked",
            countQuery = "SELECT COUNT(*) FROM review AS r WHERE r.review_id <= :top AND r.place_id=:placeId AND r.account_id NOT IN :blocked",
            nativeQuery = true
    )
    Page<Review> findAllByPlaceIdAndTopReviewId(
            @Param("top") Long topReviewId,
            @Param("placeId") Long placeId,
            @Param("blocked") Collection<Long> blockedAccountIdList,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM review AS r WHERE r.account_id=:accountId",
            countQuery = "SELECT COUNT(*) FROM review AS r WHERE r.account_id=:accountId",
            nativeQuery = true
    )
    Page<Review> findAllByAuthor(@Param("accountId") Long accountId, Pageable pageable);
}
