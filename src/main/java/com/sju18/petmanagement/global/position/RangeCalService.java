package com.sju18.petmanagement.global.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RangeCalService {
    final double RADIUS_OF_EARTH_BY_METER = 6378000;
    final double ONE_RADIAN = 180 / Math.PI;
    final double KILOMETER_IN_METER = 1000;
    final double LONGITUDE_FIRST_CORRECTION_VALUE = 111.41288;
    final double LONGITUDE_SECOND_CORRECTION_VALUE = 0.09350;
    final double LONGITUDE_THIRD_CORRECTION_VALUE = 0.00012;

    public Double calcMinLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat - (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN);
    }
    public Double calcMaxLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat + (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN);
    }
    public Double calcMinLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong - (rangeByMeter / KILOMETER_IN_METER) / (LONGITUDE_FIRST_CORRECTION_VALUE * Math.cos(originalLat / (ONE_RADIAN))
                - LONGITUDE_SECOND_CORRECTION_VALUE * Math.cos(3 * originalLat / (ONE_RADIAN)) + LONGITUDE_THIRD_CORRECTION_VALUE * Math.cos(5 * originalLat / (ONE_RADIAN)));
    }
    public Double calcMaxLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong + (rangeByMeter / KILOMETER_IN_METER) / (LONGITUDE_FIRST_CORRECTION_VALUE * Math.cos(originalLat / (ONE_RADIAN))
                - LONGITUDE_SECOND_CORRECTION_VALUE * Math.cos(3 * originalLat / (ONE_RADIAN)) + LONGITUDE_THIRD_CORRECTION_VALUE * Math.cos(5 * originalLat / (ONE_RADIAN)));
    }
}
