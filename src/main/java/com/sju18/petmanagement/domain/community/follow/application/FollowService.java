package com.sju18.petmanagement.domain.community.follow.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
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
    private final MessageSource accountMsgSrc = MessageConfig.getAccountMessageSource();
    private final MessageSource communityMsgSrc = MessageConfig.getCommunityMessageSource();
    private final FollowRepository followRepository;
    private final AccountService accountServ;
    private final NotificationPushService notificationPushService;

    @Transactional
    public void createFollow(Authentication auth, CreateFollowReqDto reqDto) throws Exception {
        Account following = accountServ.fetchCurrentAccount(auth);
        Account follower = accountServ.fetchAccountById(reqDto.getId());

        if(follower.equals(following)) {
            throw new IllegalArgumentException(
                    accountMsgSrc.getMessage("error.follow.selfFollow", null, Locale.ENGLISH)
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
                        accountMsgSrc.getMessage("error.notExist", null, Locale.ENGLISH)
                ));

        followRepository.delete(follow);
    }
}
