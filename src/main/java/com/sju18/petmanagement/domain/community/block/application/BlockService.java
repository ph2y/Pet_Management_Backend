package com.sju18.petmanagement.domain.community.block.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.dao.Block;
import com.sju18.petmanagement.domain.community.block.dao.BlockRepository;
import com.sju18.petmanagement.domain.community.block.dto.CreateBlockReqDto;
import com.sju18.petmanagement.domain.community.block.dto.DeleteBlockReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class BlockService {
    private final MessageSource accountMsgSrc = MessageConfig.getAccountMessageSource();
    private final BlockRepository blockRepository;
    private final AccountService accountServ;

    @Transactional
    public void createBlock(Authentication auth, CreateBlockReqDto reqDto) throws Exception {
        Account blocker = accountServ.fetchCurrentAccount(auth);
        Account blocked = accountServ.fetchAccountById(reqDto.getId());

        if(blocked.equals(blocker)) {
            throw new IllegalArgumentException(
                    accountMsgSrc.getMessage("error.block.selfBlock", null, Locale.ENGLISH)
            );
        }

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

    @Transactional
    public void deleteBlock(Authentication auth, DeleteBlockReqDto reqDto) throws Exception {
        Account blocker = accountServ.fetchCurrentAccount(auth);
        Account blocked = accountServ.fetchAccountById(reqDto.getId());

        Block block = blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        accountMsgSrc.getMessage("error.notExist", null, Locale.ENGLISH)
                ));

        blockRepository.delete(block);
    }
}
