package com.sju18.petmanagement.global.position;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RangeCalService {
    final double RADIUS_OF_EARTH_BY_METER = 6378000;
    final double ONE_RADIAN = 180 / Math.PI;

    public Double calcMinLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat - (rangeByMeter / RADIUS_OF_EARTH_BY_METER) *(ONE_RADIAN);
    }
    public Double calcMaxLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat + (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN);
    }
    public Double calcMinLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong + (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN) / Math.cos(originalLat * (ONE_RADIAN));
    }
    public Double calcMaxLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong - (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN) / Math.cos(originalLat * (ONE_RADIAN));
    }
}
