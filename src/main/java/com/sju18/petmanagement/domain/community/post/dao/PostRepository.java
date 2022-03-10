package com.sju18.petmanagement.domain.community.post.dao;

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
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Post> findByAuthorAndId(Account author, Long id);

    @Query(
            value = "SELECT * FROM post AS p WHERE p.pet_id=:petId",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE p.pet_id=:petId",
            nativeQuery = true
    )
    Page<Post> findAllByTaggedPetId(@Param("petId") Long taggedPetId, Pageable pageable);

    @Query(
            value = "SELECT * FROM post AS p WHERE ((:latMin <= p.geo_tag_lat AND p.geo_tag_lat <= :latMax) AND (:longMin <= p.geo_tag_long AND p.geo_tag_long <= :longMax) AND p.disclosure=\"PUBLIC\") OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE ((:latMin <= p.geo_tag_lat AND p.geo_tag_lat <= :latMax) AND (:longMin <= p.geo_tag_long AND p.geo_tag_long <= :longMax) AND p.disclosure=\"PUBLIC\") OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            nativeQuery = true
    )
    Page<Post> findAllByRadiusOption(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM post AS p WHERE p.post_id <= :top AND (((:latMin <= p.geo_tag_lat AND p.geo_tag_lat <= :latMax) AND (:longMin <= p.geo_tag_long AND p.geo_tag_long <= :longMax) AND p.disclosure=\"PUBLIC\") OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE p.post_id <= :top AND (((:latMin <= p.geo_tag_lat AND p.geo_tag_lat <= :latMax) AND (:longMin <= p.geo_tag_long AND p.geo_tag_long <= :longMax) AND p.disclosure=\"PUBLIC\") OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            nativeQuery = true
    )
    Page<Post> findAllByRadiusOptionAndTopPostId(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("longMin") Double longMin,
            @Param("longMax") Double longMax,
            @Param("top") Long topPostId,
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM post AS p WHERE (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            nativeQuery = true
    )
    Page<Post> findAllByFriendAndSelfOption(
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM post AS p WHERE p.post_id <= :top AND ((p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE p.post_id <= :top AND ((p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            nativeQuery = true
    )
    Page<Post> findAllByFriendAndSelfOptionAndTopPostId(
            @Param("top") Long topPostId,
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE p.disclosure=\"PUBLIC\" OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me",
            nativeQuery = true
    )
    Page<Post> findAllByDefaultOption(
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM post AS p WHERE p.post_id <= :top AND (p.disclosure=\"PUBLIC\" OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            countQuery = "SELECT COUNT(*) FROM post AS p WHERE p.post_id <= :top AND (p.disclosure=\"PUBLIC\" OR (p.account_id IN :friends AND p.disclosure!=\"PRIVATE\") OR p.account_id=:me)",
            nativeQuery = true
    )
    Page<Post> findAllByDefaultOptionAndTopPostId(
            @Param("top") Long topPostId,
            @Param("friends") Collection<Long> friendAccountIdList,
            @Param("me") Long myAccountId,
            Pageable pageable
    );
}
