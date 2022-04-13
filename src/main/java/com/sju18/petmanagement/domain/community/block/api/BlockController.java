package com.sju18.petmanagement.domain.community.block.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.application.BlockService;
import com.sju18.petmanagement.domain.community.block.dao.Block;
import com.sju18.petmanagement.domain.community.block.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class BlockController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final BlockService blockServ;

    @PostMapping("/api/community/block/create")
    public ResponseEntity<?> createBlock(Authentication auth, @Valid @RequestBody CreateBlockReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            blockServ.createBlock(auth, reqDto);
        }
        catch(Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateBlockResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.block.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateBlockResDto(dtoMetadata));
    }

    // 차단당한 사용자 리스트 Fetch
    @PostMapping("/api/community/blocked/fetch")
    public ResponseEntity<?> fetchBlocked(Authentication auth, @Valid @RequestBody FetchBlockedReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Account> blockedList;

        try {
            blockedList = blockServ.fetchBlocked(auth).stream().map(Block::getBlocked).collect(Collectors.toList());
        }
        catch(Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchBlockedResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.blocked.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchBlockedResDto(dtoMetadata, blockedList));
    }

    @PostMapping("/api/community/block/delete")
    public ResponseEntity<?> deleteBlock(Authentication auth, @Valid @RequestBody DeleteBlockReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            blockServ.deleteBlock(auth, reqDto);
        }
        catch(Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteBlockResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.block.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteBlockResDto(dtoMetadata));
    }
}
