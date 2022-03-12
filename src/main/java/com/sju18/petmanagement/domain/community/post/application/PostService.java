package com.sju18.petmanagement.domain.community.post.application;

import com.google.gson.Gson;
import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.follow.dao.Follow;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.domain.community.post.dao.PostRepository;
import com.sju18.petmanagement.domain.community.post.dto.*;
import com.sju18.petmanagement.domain.community.follow.application.FollowService;
import com.sju18.petmanagement.domain.pet.pet.application.PetService;
import com.sju18.petmanagement.domain.pet.pet.dao.Pet;
import com.sju18.petmanagement.global.email.EmailService;
import com.sju18.petmanagement.global.firebase.NotificationPushService;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.position.RangeCalService;
import com.sju18.petmanagement.global.storage.FileMetadata;
import com.sju18.petmanagement.global.storage.FileService;
import com.sju18.petmanagement.global.storage.FileType;
import com.sju18.petmanagement.global.storage.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final PostRepository postRepository;
    private final RangeCalService rangeCalService;
    private final AccountService accountServ;
    private final PetService petServ;
    private final FollowService followServ;
    private final FileService fileServ;
    private final NotificationPushService notificationPushService;
    private final EmailService emailServ;

    // CREATE
    @Transactional
    public Long createPost(Authentication auth, CreatePostReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Pet taggedPet = petServ.fetchPetById(auth, reqDto.getPetId());

        // 받은 사용자 정보와 새 입력 정보로 새 게시물 정보 생성
        Post post = Post.builder()
                .author(author)
                .pet(taggedPet)
                .contents(reqDto.getContents())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .serializedHashTags(String.join(",", reqDto.getHashTags()))
                .disclosure(reqDto.getDisclosure())
                .geoTagLat(reqDto.getGeoTagLat().doubleValue())
                .geoTagLong(reqDto.getGeoTagLong().doubleValue())
                .build();
        
        // save
        postRepository.save(post);
        
        // 게시물 파일 저장소 생성
        fileServ.createPostFileStorage(post.getId());

        // 게시물을 생성한 유저를 팔로우 하는 모든 유저들에게 알림 보내기. 단, 본인일 경우 제외
        List<Account> pushSubjectAccounts = followServ.fetchFollowing(auth).stream()
                .filter(follow -> !follow.getFollowing().equals(author))
                .map(Follow::getFollowing)
                .collect(Collectors.toList());

        notificationPushService.sendToMultipleDevice(
                msgSrc.getMessage("notification.post.title", null, Locale.KOREA),
                msgSrc.getMessage("notification.post.body", new String[]{author.getNickname()}, Locale.KOREA),
                pushSubjectAccounts);

        // 게시물 id 반환
        return post.getId();
    }


    // READ
    @Transactional(readOnly = true)
    public Page<Post> fetchPostByRadius(Authentication auth, Double currentLat, Double currentLong, Integer pageIndex, Long topPostId) {
        Account author = accountServ.fetchCurrentAccount(auth);
        // 기본 조건에 따른 최신 게시물 인출 (커뮤니티 메인화면 조회시)
        // 조건: author의
        //      mapSearchRadius = n (n > 0): mapSearchRadius 내 모든 공개 포스트 + 본인 포스트 + 친구 포스트
        //      mapSearchRadius = 0 : 본인 포스트 + 친구 포스트
        // 추가조건: 만약 topPostId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 게시물만 검색
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "post_id");

        if(author.getMapSearchRadius() != 0D) {
            Double latMin = rangeCalService.calcMinLatForRange(currentLat, author.getMapSearchRadius());
            Double latMax = rangeCalService.calcMaxLatForRange(currentLat, author.getMapSearchRadius());
            Double longMin = rangeCalService.calcMinLongForRange(currentLat, currentLong, author.getMapSearchRadius());
            Double longMax = rangeCalService.calcMaxLongForRange(currentLat, currentLong, author.getMapSearchRadius());

            if (topPostId != null) {
                return postRepository
                        .findAllByRadiusOptionAndTopPostId(latMin, latMax, longMin, longMax, topPostId, followServ.fetchFollower(author), author.getId(), pageQuery);
            } else {
                return postRepository
                        .findAllByRadiusOption(latMin, latMax, longMin, longMax, followServ.fetchFollower(author), author.getId(), pageQuery);
            }
        }
        else {
            if (topPostId != null) {
                return postRepository
                        .findAllByFriendAndSelfOptionAndTopPostId(topPostId, followServ.fetchFollower(author), author.getId(), pageQuery);
            } else {
                return postRepository
                        .findAllByFriendAndSelfOption(followServ.fetchFollower(author), author.getId(), pageQuery);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Post> fetchPostByDefault(Authentication auth, Integer pageIndex, Long topPostId) {
        Account author = accountServ.fetchCurrentAccount(auth);
        // 기본 조건에 따른 최신 게시물 인출 (커뮤니티 메인화면 조회시)
        // 조건: 가장 최신의 전체 공개 게시물 또는 친구의 게시물 10개 조회
        // 추가조건: 만약 topPostId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 게시물만 검색
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "post_id");

        if (topPostId != null) {
            return postRepository
                    .findAllByDefaultOptionAndTopPostId(topPostId, followServ.fetchFollower(author), author.getId(), pageQuery);
        } else {
            return postRepository
                    .findAllByDefaultOption(followServ.fetchFollower(author), author.getId(), pageQuery);
        }
    }

    @Transactional(readOnly = true)
    public Page<Post> fetchPostByPet(Long petId, Integer pageIndex) {
        // 태그된 펫으로 게시물 인출 (펫 피드 조회시)
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex,10, Sort.Direction.DESC, "post_id");

        return postRepository.findAllByTaggedPetId(petId, pageQuery);
    }

    @Transactional(readOnly = true)
    public Post fetchPostById(Long postId) throws Exception {
        // 게시물 고유번호로 게시물 인출 (게시물 단일 불러오기시 사용)
        return postRepository.findById(postId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));
    }

    public byte[] fetchPostImage(Long postId, Integer fileIndex, Integer imageType) throws Exception {
        Post currentPost = postRepository.findById(postId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        // 이미지 파일 인출
        return fileServ.readFileFromFileMetadataListJson(currentPost.getImageAttachments(), fileIndex, imageType);
    }

    public ResponseEntity<byte[]> fetchPostVideo(String fileUrl, String range) throws Exception {
        Long fileSize = fileServ.getFileSize(fileUrl);
        String fileType = fileServ.getFileExtension(fileUrl);

        long rangeStart = 0;
        long rangeEnd;
        byte[] data;

        // HTTP Range 필드가 비어있으면 파일 전체 fetch
        if (range == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "video/" + fileType)
                    .header("Content-Length", String.valueOf(fileSize))
                    .body(fileServ.readByteRange(fileUrl, rangeStart, fileSize - 1)); // Read the object and convert it as bytes
        }

        // 요청받은 Range 에 따라 파일을 나누어 fetch
        String[] ranges = range.split("-");
        rangeStart = Long.parseLong(ranges[0].substring(6));
        if (ranges.length > 1) {
            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            rangeEnd = fileSize - 1;
        }
        if (fileSize < rangeEnd) {
            rangeEnd = fileSize - 1;
        }

        System.out.println("Video Streaming... | Range: bytes=" + rangeStart + "-" + rangeEnd);
        data = fileServ.readByteRange(fileUrl, rangeStart, rangeEnd);

        String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header("Content-Type", "video/" + fileType)
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", contentLength)
                .header("Content-Range", "bytes" + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .body(data);
    }

    public byte[] fetchPostFile(Long postId, Integer fileIndex) throws Exception {
        Post currentPost = postRepository.findById(postId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        // 일반 파일 인출
        return fileServ.readFileFromFileMetadataListJson(currentPost.getFileAttachments(), fileIndex, ImageUtil.NOT_IMAGE);
    }

    // UPDATE
    @Transactional
    public void updatePost(Authentication auth, UpdatePostReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물 id로 게시물 정보 수정
        Account author = accountServ.fetchCurrentAccount(auth);
        Post currentPost = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        if (!reqDto.getPetId().equals(currentPost.getPet().getId())) {
            Pet taggedPet = petServ.fetchPetById(auth, reqDto.getPetId());
            currentPost.setPet(taggedPet);
        }
        if (!reqDto.getContents().equals(currentPost.getContents())) {
            currentPost.setContents(reqDto.getContents());
        }
        if (!String.join(",", reqDto.getHashTags()).equals(currentPost.getSerializedHashTags())) {
            currentPost.setSerializedHashTags(String.join(",", reqDto.getHashTags()));
        }
        if (!reqDto.getDisclosure().equals(currentPost.getDisclosure())) {
            currentPost.setDisclosure(reqDto.getDisclosure());
        }
        currentPost.setEdited(true);

        // save
        postRepository.save(currentPost);
    }

    @Transactional
    public List<FileMetadata> updatePostFile(Authentication auth, UpdatePostFileReqDto reqDto, FileType fileType) throws Exception {
        // 기존 게시물 정보 로드
        Account author = accountServ.fetchCurrentAccount(auth);
        Post currentPost = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        // 첨부파일 인출
        List<MultipartFile> uploadedFileList = reqDto.getFileList();

        List<FileMetadata> fileMetadataList;
        if (uploadedFileList.size() == 0) {
            throw new Exception(
                    msgSrc.getMessage("error.fileList.empty", null, Locale.ENGLISH)
            );
        }

        switch (fileType) {
            case GENERAL_FILE:
                // 해당 게시물의 파일 스토리지에 일반 파일 저장
                fileMetadataList = fileServ.savePostFileAttachments(reqDto.getId(), uploadedFileList);

                // 파일정보 DB 데이터 업데이트
                currentPost.setFileAttachments(new Gson().toJson(fileMetadataList));
                postRepository.save(currentPost);
                return fileMetadataList;
            case IMAGE_FILE:
                // 해당 게시물의 이미지 스토리지에 이미지 파일 저장
                fileMetadataList = fileServ.savePostImageAttachments(reqDto.getId(), uploadedFileList);

                // 파일정보 DB 데이터 업데이트
                currentPost.setImageAttachments(new Gson().toJson(fileMetadataList));
                postRepository.save(currentPost);
                return fileMetadataList;
            case VIDEO_FILE:
                // 해당 게시물의 비디오 스토리지에 비디오 파일 저장
                fileMetadataList = fileServ.savePostVideoAttachments(reqDto.getId(), uploadedFileList);

                // 파일정보 DB 데이터 업데이트
                currentPost.setVideoAttachments(new Gson().toJson(fileMetadataList));
                postRepository.save(currentPost);
                return fileMetadataList;
            default:
                throw new Exception(
                        msgSrc.getMessage("error.post.invalidFileType", null, Locale.ENGLISH)
                );
        }
    }

    // DELETE
    @Transactional
    public void deletePost(Authentication auth, DeletePostReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 게시물 id로 게시물 정보 삭제
        Account author = accountServ.fetchCurrentAccount(auth);
        Post post = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));
        fileServ.deletePostFileStorage(post.getId());
        postRepository.delete(post);
    }

    @Transactional
    public void deletePostFile(Authentication auth, DeletePostFileReqDto reqDto, FileType fileType) throws Exception {
        // 기존 게시물 정보 로드
        Account author = accountServ.fetchCurrentAccount(auth);
        Post currentPost = postRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        switch (fileType) {
            case GENERAL_FILE:
                // 기존 게시물의 모든 일반 파일 삭제
                fileServ.deletePostFiles(currentPost.getFileAttachments(), ImageUtil.NOT_IMAGE);

                // 기존 게시물의 fileAttachments 컬럼 null 설정 후 업데이트
                currentPost.setFileAttachments(null);
                break;
            case IMAGE_FILE:
                // 기존 게시물의 모든 이미지 파일 삭제
                fileServ.deletePostFiles(currentPost.getImageAttachments(), ImageUtil.GENERAL_IMAGE);

                // 기존 게시물의 imageAttachments 컬럼 null 설정 후 업데이트
                currentPost.setImageAttachments(null);
                break;
            case VIDEO_FILE:
                // 기존 게시물의 모든 비디오 파일 삭제
                fileServ.deletePostFiles(currentPost.getVideoAttachments(), ImageUtil.NOT_IMAGE);

                // 기존 게시물의 videoAttachments 컬럼 null 설정 후 업데이트
                currentPost.setVideoAttachments(null);
                break;
            default:
                throw new Exception(
                        msgSrc.getMessage("error.post.invalidFileType", null, Locale.ENGLISH)
                );
        }
        postRepository.save(currentPost);
    }

    @Transactional
    public void reportPost(Long postId) throws Exception {
        // 기존 게시물 정보 로드
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.post.notExists", null, Locale.ENGLISH)
                ));

        emailServ.sendContentReportNotifyMessage("post", post.getId(), post.getAuthor().getId(), post.getContents());
    }
}
