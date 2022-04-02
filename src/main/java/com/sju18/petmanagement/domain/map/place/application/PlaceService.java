package com.sju18.petmanagement.domain.map.place.application;

import com.sju18.petmanagement.domain.map.place.dao.CategoryCode;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.domain.map.place.dao.PlaceRepository;
import com.sju18.petmanagement.domain.map.place.dto.CreatePlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.DeletePlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.FetchPlaceReqDto;
import com.sju18.petmanagement.domain.map.place.dto.UpdatePlaceReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.position.RangeCalService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class PlaceService {
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final PlaceRepository placeRepository;
    private final RangeCalService rangeCalService;

    public static final int INCREMENT = 1;
    public static final int NO_CHANGE = 0;
    public static final int DECREMENT = -1;

    public static final double DISTRICT_SCALE = 10000.0;
    public static final double CITY_SCALE = 30000.0;

    public static final double CITY_SCALE_SAMPLE_SIZE = 0.3;
    public static final double METROPOLITAN_SCALE_SAMPLE_SIZE = 0.1;

    // CREATE
    @Transactional
    public void createPlace(CreatePlaceReqDto reqDto) {
        // 받은 입력 정보로 새 장소 정보 생성
        Place place = Place.builder()
                .name(reqDto.getName())
                .categoryCode(reqDto.getCategoryCode())
                .latitude(reqDto.getLatitude().doubleValue())
                .longitude(reqDto.getLongitude().doubleValue())
                .description(reqDto.getDescription())
                .phone(reqDto.getPhone())
                .averageRating(null)
                .reviewCount(0L)
                .operationDay(reqDto.getOperationDay())
                .operationHour(reqDto.getOperationHour())
                .build();

        // save
        placeRepository.save(place);
    }

    // READ
    @Transactional(readOnly = true)
    public Place fetchPlaceById(Long placeId) throws Exception {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
    }
    @Transactional(readOnly = true)
    public List<Place> fetchPlaceByKeywordAndDistance(FetchPlaceReqDto reqDto) {
        List<Double> distanceRange = calDistanceRange(reqDto);
        double searchRange = reqDto.getRange().doubleValue();

        if(searchRange <= DISTRICT_SCALE) {
            if(EnumUtils.isValidEnum(CategoryCode.class, reqDto.getKeyword())) {
                return placeRepository.findAllByCategoryCodeAndDistance(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword());
            }
            else {
                return placeRepository.findAllByKeywordAndDistance(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword());
            }
        }
        else if(DISTRICT_SCALE < searchRange && searchRange <= CITY_SCALE) {
            if(EnumUtils.isValidEnum(CategoryCode.class, reqDto.getKeyword())) {
                return placeRepository.findAllByCategoryCodeAndDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword(), CITY_SCALE_SAMPLE_SIZE);
            }
            else {
                return placeRepository.findAllByKeywordAndDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword(), CITY_SCALE_SAMPLE_SIZE);
            }
        }
        else {
            if(EnumUtils.isValidEnum(CategoryCode.class, reqDto.getKeyword())) {
                return placeRepository.findAllByCategoryCodeAndDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword(), METROPOLITAN_SCALE_SAMPLE_SIZE);
            }
            else {
                return placeRepository.findAllByKeywordAndDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), reqDto.getKeyword(), METROPOLITAN_SCALE_SAMPLE_SIZE);
            }
        }
    }
    @Transactional(readOnly = true)
    public List<Place> fetchPlaceByDistance(FetchPlaceReqDto reqDto) {
        List<Double> distanceRange = calDistanceRange(reqDto);
        double searchRange = reqDto.getRange().doubleValue();

        if(searchRange <= DISTRICT_SCALE) {
            return placeRepository.findAllByDistance(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3));
        }
        else if(DISTRICT_SCALE < searchRange && searchRange <= CITY_SCALE) {
            return placeRepository.findAllByDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), CITY_SCALE_SAMPLE_SIZE);
        }
        else {
            return placeRepository.findAllByDistanceWithRandomSampling(distanceRange.get(0), distanceRange.get(1), distanceRange.get(2), distanceRange.get(3), METROPOLITAN_SCALE_SAMPLE_SIZE);
        }
    }

    private List<Double> calDistanceRange(FetchPlaceReqDto reqDto) {
        List<Double> distanceRange = new ArrayList<>();

        Double currentLat = reqDto.getCurrentLat().doubleValue();
        Double currentLong = reqDto.getCurrentLong().doubleValue();
        Double range = reqDto.getRange().doubleValue();

        Double latMin = rangeCalService.calcMinLatForRange(currentLat, range);
        Double latMax = rangeCalService.calcMaxLatForRange(currentLat, range);
        Double longMin = rangeCalService.calcMinLongForRange(currentLat, currentLong, range);
        Double longMax = rangeCalService.calcMaxLongForRange(currentLat, currentLong, range);

        distanceRange.add(latMin);
        distanceRange.add(latMax);
        distanceRange.add(longMin);
        distanceRange.add(longMax);

        return distanceRange;
    }

    // UPDATE
    @Transactional
    public void updatePlace(UpdatePlaceReqDto reqDto) throws Exception {
        Place currentPlace = placeRepository.findById(reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
        if (!reqDto.getName().equals(currentPlace.getName())) {
            currentPlace.setName(reqDto.getName());
        }
        if (!reqDto.getCategoryCode().equals(currentPlace.getCategoryCode())) {
            currentPlace.setCategoryCode(reqDto.getCategoryCode());
        }
        if (reqDto.getLatitude().doubleValue() != currentPlace.getLatitude()) {
            currentPlace.setLatitude(reqDto.getLatitude().doubleValue());
        }
        if (reqDto.getLongitude().doubleValue() != currentPlace.getLongitude()) {
            currentPlace.setLongitude(reqDto.getLongitude().doubleValue());
        }
        if (!reqDto.getDescription().equals(currentPlace.getDescription())) {
            currentPlace.setDescription(reqDto.getDescription());
        }
        if (!reqDto.getPhone().equals(currentPlace.getPhone())) {
            currentPlace.setPhone(reqDto.getPhone());
        }
        if (!reqDto.getOperationDay().equals(currentPlace.getOperationDay())) {
            currentPlace.setOperationDay(reqDto.getOperationDay());
        }
        if (!reqDto.getOperationHour().equals(currentPlace.getOperationHour())) {
            currentPlace.setOperationHour(reqDto.getOperationHour());
        }

        // save
        placeRepository.save(currentPlace);
    }
    @Transactional
    public void updatePlaceAverageRatingAndReviewCount(Long placeId, Double placeRating, int reviewOperator) throws Exception {
        Place currentPlace = placeRepository.findById(placeId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
        currentPlace.setAverageRating(placeRating);
        currentPlace.setReviewCount(currentPlace.getReviewCount() + reviewOperator);
        placeRepository.save(currentPlace);
    }
    // DELETE
    @Transactional
    public void deletePlace(DeletePlaceReqDto reqDto) throws Exception {
        // 받은 장소 id로 장소 정보 삭제
        Place place = placeRepository.findById(reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.place.notExists", null, Locale.ENGLISH)
                ));
        placeRepository.delete(place);
    }
}
