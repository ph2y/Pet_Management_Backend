package com.sju18.petmanagement.domain.community.follow.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findAllByFollowingId(Long followingId);
    List<Follow> findAllByFollowerId(Long followerId);
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
