package com.sju18.petmanagement.domain.community.block.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAllByBlockerId(Long blockerId);
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
