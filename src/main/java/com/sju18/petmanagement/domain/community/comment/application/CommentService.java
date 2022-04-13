package com.sju18.petmanagement.domain.community.comment.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.application.BlockService;
import com.sju18.petmanagement.domain.community.comment.dao.Comment;
import com.sju18.petmanagement.domain.community.comment.dao.CommentRepository;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.domain.community.comment.dto.CreateCommentReqDto;
import com.sju18.petmanagement.domain.community.comment.dto.DeleteCommentReqDto;
import com.sju18.petmanagement.domain.community.comment.dto.FetchCommentReqDto;
import com.sju18.petmanagement.domain.community.comment.dto.UpdateCommentReqDto;
import com.sju18.petmanagement.domain.community.post.application.PostService;
import com.sju18.petmanagement.global.email.EmailService;
import com.sju18.petmanagement.global.firebase.NotificationPushService;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final CommentRepository commentRepository;
    private final AccountService accountServ;
    private final PostService postServ;
    private final BlockService blockServ;
    private final NotificationPushService notificationPushService;
    private final EmailService emailServ;

    // CREATE
    @Transactional
    public Long createComment(Authentication auth, CreateCommentReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Post commentedPost = null;
        Comment repliedComment = null;
        if (reqDto.getParentCommentId() != null) {
            repliedComment = this.fetchCommentById(reqDto.getParentCommentId());
        } else {
            commentedPost = postServ.fetchPostById(reqDto.getPostId());
        }

        // 받은 사용자 정보와 새 입력 정보로 새 댓글 정보 생성
        Comment comment = Comment.builder()
                .author(author)
                .post(commentedPost)
                .postId(commentedPost != null ? commentedPost.getId() : null)
                .parentComment(repliedComment)
                .parentCommentId(repliedComment != null ? repliedComment.getId() : null)
                .childCommentCnt(0)
                .contents(reqDto.getContents())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .build();

        // 댓답글인 경우 부모 댓글의 댓답글 카운트 +1
        if (repliedComment != null) {
            repliedComment.setChildCommentCnt(repliedComment.getChildCommentCnt() + 1);
        }

        // save
        commentRepository.save(comment);

        // 댓글을 단 대상 (포스트 작성 유저 OR 댓글 작성 유저)에게 댓글 알림 보내기, 단 본인이 본인의 포스트에 댓글을 적을 경우 알림 X
        if(commentedPost != null && !commentedPost.getAuthor().equals(author)) {
            notificationPushService.sendToSingleDevice(msgSrc.getMessage("notification.comment.post.title", null, Locale.KOREA),
                    msgSrc.getMessage("notification.comment.post.body", new String[]{author.getNickname()}, Locale.KOREA),
                    commentedPost.getAuthor());
        }
        else if (repliedComment != null && !repliedComment.getAuthor().equals(author)) {
            notificationPushService.sendToSingleDevice(msgSrc.getMessage("notification.comment.replied.title", null, Locale.KOREA),
                    msgSrc.getMessage("notification.comment.replied.body", new String[]{author.getNickname()}, Locale.KOREA),
                    repliedComment.getAuthor());
        }
        
        // 댓글 id 반환
        return comment.getId();
    }

    // READ
    @Transactional(readOnly = true)
    public Page<Comment> fetchCommentByPostId(Authentication auth, FetchCommentReqDto reqDto) {
        Account author = accountServ.fetchCurrentAccount(auth);
        // 기본 조건에 따른 최신 댓글 인출 (게시물 댓글화면 조회시)
        // 조건: 가장 최신 댓글 50개 조회
        // 추가조건: 만약 fromId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 댓글만 검색
        if (reqDto.getPageIndex() == null) {
            reqDto.setPageIndex(0);
        }
        Pageable pageQuery = PageRequest.of(reqDto.getPageIndex(), 50, Sort.Direction.DESC, "comment_id");

        if (reqDto.getTopCommentId() != null) {
            return commentRepository
                    .findAllByPostIdAndTopCommentId(reqDto.getTopCommentId(), reqDto.getPostId(), blockServ.fetchBlocked(author), pageQuery);
        } else {
            return commentRepository.findAllByPostId(reqDto.getPostId(), blockServ.fetchBlocked(author), pageQuery);
        }
    }

    @Transactional(readOnly = true)
    public Page<Comment> fetchCommentByParentCommentId(Authentication auth, FetchCommentReqDto reqDto) {
        Account author = accountServ.fetchCurrentAccount(auth);
        // 기본 조건에 따른 최신 댓답글 인출 (댓글 답글화면 조회시)
        // 조건: 가장 최신 댓답글 50개 조회
        // 추가조건: 만약 fromId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 댓답글만 검색
        // 추가조건: 차단한 사용자의 댓글은 제외
        if (reqDto.getPageIndex() == null) {
            reqDto.setPageIndex(0);
        }
        Pageable pageQuery = PageRequest.of(reqDto.getPageIndex(), 50, Sort.Direction.DESC, "comment_id");

        if (reqDto.getTopCommentId() != null) {
            return commentRepository
                    .findAllByParentCommentIdAndTopCommentId(
                            reqDto.getTopCommentId(), reqDto.getParentCommentId(), blockServ.fetchBlocked(author), pageQuery
                    );
        } else {
            return commentRepository.findAllByParentCommentId(reqDto.getParentCommentId(), blockServ.fetchBlocked(author), pageQuery);
        }
    }

    @Transactional
    public Comment fetchCommentById(Long commentId) throws Exception {
        // 댓글/댓답글 고유번호로 댓글/댓답글 인출 (댓글/댓답글 단일 불러오기시 사용)
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));
    }

    // UPDATE
    @Transactional
    public void updateComment(Authentication auth, UpdateCommentReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 댓글/댓답글 id로 댓글/댓답글 정보 수정
        Account author = accountServ.fetchCurrentAccount(auth);
        Comment currentComment = commentRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));

        if(reqDto.getContents() != null && !reqDto.getContents().equals(currentComment.getContents())) {
            currentComment.setContents(reqDto.getContents());
        }
        currentComment.setEdited(true);

        // save
        commentRepository.save(currentComment);
    }

    // DELETE
    @Transactional
    public void deleteComment(Authentication auth, DeleteCommentReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 댓글/댓답글 id로 댓글/댓답글 정보 삭제
        Account author = accountServ.fetchCurrentAccount(auth);
        Comment comment = commentRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));

        // 댓답글인 경우 부모 댓글의 댓답글 카운트 -1
        Comment parentComment = comment.getParentComment();
        if (parentComment != null) {
            parentComment.setChildCommentCnt(parentComment.getChildCommentCnt() - 1);
            commentRepository.save(parentComment);
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void reportComment(Long commentId) throws Exception {
        // 기존 코멘트 정보 로드
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.comment.notExists", null, Locale.ENGLISH)
                ));

        emailServ.sendContentReportNotifyMessage("comment", comment.getId(), comment.getAuthor().getId(), comment.getContents());
    }
}
