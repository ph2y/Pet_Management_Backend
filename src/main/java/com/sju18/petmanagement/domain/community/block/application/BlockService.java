package com.sju18.petmanagement.domain.community.block.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.dao.Block;
import com.sju18.petmanagement.domain.community.block.dao.BlockRepository;
import com.sju18.petmanagement.domain.community.block.dto.CreateBlockReqDto;
import com.sju18.petmanagement.domain.community.block.dto.DeleteBlockReqDto;
import com.sju18.petmanagement.domain.community.follow.dao.FollowRepository;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BlockService {
    private final MessageSource communityMsgSrc = MessageConfig.getCommunityMessageSource();
    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final AccountService accountServ;

    @Transactional
    public void createBlock(Authentication auth, CreateBlockReqDto reqDto) throws Exception {
        Account blocker = accountServ.fetchCurrentAccount(auth);
        Account blocked = accountServ.fetchAccountById(reqDto.getId());

        // 이미 존재하는 경우 예외처리
        if(blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.block.exists", null, Locale.ENGLISH)
            );
        }

        // 본인을 Block 할 경우 예외처리
        if(blocked.equals(blocker)) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.block.selfBlock", null, Locale.ENGLISH)
            );
        }

        // 기존에 Follow Relationship 이 있을 경우 relationship 삭제
        followRepository.findByFollowerIdAndFollowingId(blocker.getId(), blocked.getId()).ifPresent(followRepository::delete);
        followRepository.findByFollowerIdAndFollowingId(blocked.getId(), blocker.getId()).ifPresent(followRepository::delete);

        // Block Relationship 생성
        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        blockRepository.save(block);
    }

    @Transactional(readOnly = true)
    public List<Block> fetchBlocked(Authentication auth) {
        Account blocker = accountServ.fetchCurrentAccount(auth);

        return new ArrayList<>(blockRepository.findAllByBlockerId(blocker.getId()));
    }

    @Transactional(readOnly = true)
    public List<Long> fetchBlocked(Account account) {
        List<Long> blocked = blockRepository.findAllByBlockerId(account.getId())
                .stream().map(block -> block.getBlocked().getId())
                .collect(Collectors.toList());

        // SQL 에서 NOT IN 절에 사용할 것이기 때문에, 빈 배열로 넘기지 않고 0L 을 남아 반환한다.
        // 빈 배열로 넘기면 SQL 에서 NOT IN (NULL) 로 해석하여 0개의 row 를 반환하게 된다.
        if(blocked.isEmpty()) blocked.add(0L);

        return blocked;
    }

    @Transactional
    public void deleteBlock(Authentication auth, DeleteBlockReqDto reqDto) throws Exception {
        Account blocker = accountServ.fetchCurrentAccount(auth);
        Account blocked = accountServ.fetchAccountById(reqDto.getId());

        Block block = blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        communityMsgSrc.getMessage("error.block.notExists", null, Locale.ENGLISH)
                ));

        blockRepository.delete(block);
    }
}
