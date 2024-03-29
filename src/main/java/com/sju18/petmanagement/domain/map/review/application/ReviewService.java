package com.sju18.petmanagement.domain.map.review.application;

import com.google.gson.Gson;
import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.block.application.BlockService;
import com.sju18.petmanagement.domain.map.review.dao.Review;
import com.sju18.petmanagement.domain.map.review.dao.ReviewRepository;
import com.sju18.petmanagement.domain.map.place.application.PlaceService;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.domain.map.review.dto.CreateReviewReqDto;
import com.sju18.petmanagement.domain.map.review.dto.DeleteReviewReqDto;
import com.sju18.petmanagement.domain.map.review.dto.UpdateReviewMediaReqDto;
import com.sju18.petmanagement.domain.map.review.dto.UpdateReviewReqDto;
import com.sju18.petmanagement.global.email.EmailService;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileMetadata;
import com.sju18.petmanagement.global.storage.FileService;
import com.sju18.petmanagement.global.storage.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final ReviewRepository reviewRepository;
    private final AccountService accountServ;
    private final BlockService blockServ;
    private final PlaceService placeServ;
    private final FileService fileServ;
    private final EmailService emailServ;

    // CREATE
    @Transactional
    public Long createReview(Authentication auth, CreateReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Place place = placeServ.fetchPlaceById(reqDto.getPlaceId());

        // 받은 사용자 정보와 입력 정보로 새 장소 즐겨찾기 정보 생성
        Review review = Review.builder()
                .author(author)
                .place(place)
                .placeId(place.getId())
                .contents(reqDto.getContents())
                .rating(reqDto.getRating())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .build();

        // save
        reviewRepository.save(review);

        // place 평균평점 및 리뷰 개수 갱신
        placeServ.updatePlaceAverageRatingAndReviewCount(place.getId(), this.fetchAverageRatingByPlaceId(place.getId()), PlaceService.INCREMENT);

        // 리뷰 파일 저장소 생성
        fileServ.createReviewFileStorage(review.getId());

        // 리뷰 id 반환
        return review.getId();
    }

    // READ
    @Transactional(readOnly = true)
    public Page<Review> fetchReviewByPlaceId(Authentication auth, Long placeId, Integer pageIndex, Long topReviewId) {
        Account author = accountServ.fetchCurrentAccount(auth);
        // 특정 장소의 리뷰 리스트 인출
        // 조건: 가장 최신 리뷰 10개 조회
        // 추가조건: 만약 topReviewId(최초 로딩 시점)를 설정했다면 해당 시점 이전의 리뷰만 검색
        // 추가조건: 차단한 사용자의 리뷰는 제외
        if (pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "review_id");

        if (topReviewId != null) {
            return reviewRepository.findAllByPlaceIdAndTopReviewId(topReviewId, placeId, blockServ.fetchBlocked(author), pageQuery);
        }
        else {
            return reviewRepository.findAllByPlaceId(placeId, blockServ.fetchBlocked(author), pageQuery);
        }
    }

    @Transactional(readOnly = true)
    public Page<Review> fetchReviewByAuthor(Long authorId, Integer pageIndex) throws Exception {
        if(pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "review_id");

        return reviewRepository.findAllByAuthor(authorId, pageQuery);
    }

    @Transactional(readOnly = true)
    public Page<Review> fetchMyReview(Authentication auth, Integer pageIndex) {
        // 사용자가 작성한 리뷰 리스트 인출
        Account author = accountServ.fetchCurrentAccount(auth);

        if(pageIndex == null) {
            pageIndex = 0;
        }
        Pageable pageQuery = PageRequest.of(pageIndex, 10, Sort.Direction.DESC, "review_id");

        return reviewRepository.findAllByAuthor(author.getId(), pageQuery);
    }

    @Transactional(readOnly = true)
    public Review fetchReviewById(Long reviewId) throws Exception {
        // 개별 리뷰 인출
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
    }

    @Transactional(readOnly = true)
    public Review fetchReviewByPlaceIdAndAuthorId(Long placeId, Long authorId) throws Exception {
        // 특정 장소에서 특정 회원의 리뷰 조회 요청
        return reviewRepository.findByPlaceIdAndAuthorId(placeId, authorId)
                .orElseThrow(() -> new Exception(
                   msgSrc.getMessage("error.review.myReviewOfPlace.notExists", null, Locale.ENGLISH)
                ));
    }

    @Transactional(readOnly = true)
    public Double fetchAverageRatingByPlaceId(Long placeId) throws Exception {
        List<Integer> ratingList = reviewRepository.findAllByPlaceId(placeId, Collections.singletonList(0L),null).getContent()
                .stream().map(Review::getRating).collect(Collectors.toList());
        if(ratingList.size() == 0) return 0.0;
        else return ratingList.stream().mapToDouble(rating -> rating).average()
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.avgCalcFailure", null, Locale.ENGLISH)
                ));
    }

    public byte[] fetchReviewMedia(Authentication auth, Long reviewId, Integer fileIndex) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Review currentReview = reviewRepository.findByAuthorAndId(author, reviewId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));

        // 미디어 파일 인출
        return fileServ.readFileFromFileMetadataListJson(currentReview.getMediaAttachments(), fileIndex, ImageUtil.GENERAL_IMAGE);
    }

    // UPDATE
    @Transactional
    public void updateReview(Authentication auth, UpdateReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 입력 정보로 장소 즐겨찾기 정보 수정
        Review currentReview = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
        if (!reqDto.getContents().equals(currentReview.getContents())) {
            currentReview.setContents(reqDto.getContents());
        }
        if (!reqDto.getRating().equals(currentReview.getRating())) {
            currentReview.setRating(reqDto.getRating());
        }
        currentReview.setEdited(true);

        // save
        reviewRepository.save(currentReview);

        // place 평균평점 갱신
        placeServ.updatePlaceAverageRatingAndReviewCount(
                currentReview.getPlaceId(), this.fetchAverageRatingByPlaceId(currentReview.getPlaceId()), PlaceService.NO_CHANGE
        );
    }

    @Transactional
    public List<FileMetadata> updateReviewMedia(Authentication auth, UpdateReviewMediaReqDto reqDto) throws Exception {
        // 기존 리뷰 정보 로드
        Account author = accountServ.fetchCurrentAccount(auth);
        Review currentReview = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));

        // 첨부파일 인출
        List<MultipartFile> uploadedFileList = reqDto.getFileList();

        // 해당 리뷰의 미디어 스토리지에 미디어 파일 저장
        List<FileMetadata> mediaFileMetadataList = null;
        if (uploadedFileList.size() != 0) {
            mediaFileMetadataList = fileServ.saveReviewAttachments(reqDto.getId(), uploadedFileList);

            // 파일정보 DB 데이터 업데이트
            currentReview.setMediaAttachments(new Gson().toJson(mediaFileMetadataList));
            reviewRepository.save(currentReview);
        }
        return mediaFileMetadataList;
    }

    // DELETE
    @Transactional
    public Integer deleteReview(Authentication auth, DeleteReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 장소 즐겨찾기 id로 장소 즐겨찾기 정보 삭제
        Review review = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
        // 삭제할 리뷰의 레이팅 따로 저장
        Integer deletedReviewRating = review.getRating();

        // 리뷰 파일 저장소 삭제
        fileServ.deleteReviewFileStorage(review.getId());
        reviewRepository.delete(review);

        // place 평균평점 갱신
        placeServ.updatePlaceAverageRatingAndReviewCount(
                review.getPlaceId(), this.fetchAverageRatingByPlaceId(review.getPlaceId()), PlaceService.DECREMENT
        );

        return deletedReviewRating;
    }

    @Transactional
    public void reportReview(Long reviewId) throws Exception {
        // 기존 리뷰 정보 로드
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));

        emailServ.sendContentReportNotifyMessage("review", review.getId(), review.getAuthor().getId(), review.getContents());
    }
}
