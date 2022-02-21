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
        System.out.printf("MinLong: %f\n", originalLong - (rangeByMeter / 1000) / (111.41288 * Math.cos(originalLat * (Math.PI / 180))
                - 0.09350 * Math.cos(3 * originalLat * (Math.PI / 180)) + 0.00012 * Math.cos(5 * originalLat * (Math.PI / 180))));

        return originalLong - (rangeByMeter / 1000) / (111.41288 * Math.cos(originalLat * (Math.PI / 180))
                - 0.09350 * Math.cos(3 * originalLat * (Math.PI / 180)) + 0.00012 * Math.cos(5 * originalLat * (Math.PI / 180)));
    }
    public Double calcMaxLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        System.out.printf("MaxLong: %f\n", originalLong + (rangeByMeter / 1000) / (111.41288 * Math.cos(originalLat * (Math.PI / 180))
                - 0.09350 * Math.cos(3 * originalLat * (Math.PI / 180)) + 0.00012 * Math.cos(5 * originalLat * (Math.PI / 180))));
        return originalLong + (rangeByMeter / 1000) / (111.41288 * Math.cos(originalLat * (Math.PI / 180))
                - 0.09350 * Math.cos(3 * originalLat * (Math.PI / 180)) + 0.00012 * Math.cos(5 * originalLat * (Math.PI / 180)));
    }
}
