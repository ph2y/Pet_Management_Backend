package com.sju18.petmanagement.domain.community.follow.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.dao.BlockRepository;
import com.sju18.petmanagement.domain.community.follow.dao.Follow;
import com.sju18.petmanagement.domain.community.follow.dao.FollowRepository;
import com.sju18.petmanagement.domain.community.follow.dto.CreateFollowReqDto;
import com.sju18.petmanagement.domain.community.follow.dto.DeleteFollowReqDto;
import com.sju18.petmanagement.global.firebase.NotificationPushService;
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
public class FollowService {
    private final MessageSource communityMsgSrc = MessageConfig.getCommunityMessageSource();
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final AccountService accountServ;
    private final NotificationPushService notificationPushService;

    @Transactional
    public void createFollow(Authentication auth, CreateFollowReqDto reqDto) throws Exception {
        Account following = accountServ.fetchCurrentAccount(auth);
        Account follower = accountServ.fetchAccountById(reqDto.getId());

        // 이미 존재하는 경우 예외처리
        if(followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId())) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.follow.exists", null, Locale.ENGLISH)
            );
        }

        // 본인을 Follow 할 경우 예외처리
        if(follower.equals(following)) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.follow.selfFollow", null, Locale.ENGLISH)
            );
        }

        // Follow 하려는 대상이 자신을 Block 한 상태인 경우 예외처리
        if(blockRepository.findByBlockerIdAndBlockedId(follower.getId(), following.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.follow.blockedByFollower", null, Locale.ENGLISH)
            );
        }
        // 본인이 Follow 하려는 대상을 Block 한 상태인 경우 예외처리
        else if(blockRepository.findByBlockerIdAndBlockedId(following.getId(), follower.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    communityMsgSrc.getMessage("error.follow.selfBlockedFollower", null, Locale.ENGLISH)
            );
        }

        // Follow Relationship 생성
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);

        // Follower 인 유저에게 팔로우 알림 보내기.
        notificationPushService.sendToSingleDevice(
                communityMsgSrc.getMessage("notification.follow.title", null, Locale.KOREA),
                communityMsgSrc.getMessage("notification.follow.body", new String[]{following.getNickname()}, Locale.KOREA),
                follower);
    }

    // 현재 사용자가 팔로잉하고 있는, 사용자가 Following 객체이고 찾는 객체가 Follower 객체인 Follow 리스트 Fetch
    @Transactional(readOnly = true)
    public List<Follow> fetchFollower(Authentication auth) {
        Account following = accountServ.fetchCurrentAccount(auth);

        return new ArrayList<>(followRepository.findAllByFollowingId(following.getId()));
    }

    @Transactional(readOnly = true)
    public List<Long> fetchFollower(Account account) {
        return new ArrayList<>(followRepository.findAllByFollowingId(account.getId()))
                .stream().map(follow -> follow.getFollower().getId())
                .collect(Collectors.toList());
    }

    // 현재 사용자를 팔로우하고 있는, 사용자가 Follower 객체이고 찾는 객체가 Following 객체인 Follow 리스트 Fetch
    @Transactional(readOnly = true)
    public List<Follow> fetchFollowing(Authentication auth) {
        Account follower = accountServ.fetchCurrentAccount(auth);

        return new ArrayList<>(followRepository.findAllByFollowerId(follower.getId()));
    }

    @Transactional
    public void deleteFollow(Authentication auth, DeleteFollowReqDto reqDto) throws Exception {
        Account following = accountServ.fetchCurrentAccount(auth);
        Account follower = accountServ.fetchAccountById(reqDto.getId());

        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        communityMsgSrc.getMessage("error.follow.notExists", null, Locale.ENGLISH)
                ));

        followRepository.delete(follow);
    }
}
