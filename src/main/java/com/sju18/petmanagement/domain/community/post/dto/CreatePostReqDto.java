package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePostReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long petId;
    @Size(max = 10000, message = "valid.post.contents.size")
    private String contents;
    @Size(max = 5, message = "valid.post.hashTags.count")
    private List<@Size(max = 20, message = "valid.post.hashTags.size") String> hashTags;
    @Pattern(
            regexp = "^(PUBLIC|PRIVATE|FRIEND)$",
            message = "valid.post.disclosure.enum"
    )
    private String disclosure;
    @DecimalMax(value = "90.0", message = "valid.post.geoTagLat.max")
    @DecimalMin(value = "-90.0", message = "valid.post.geoTagLat.min")
    private Double geoTagLat;
    @DecimalMax(value = "180.0", message = "valid.post.geoTagLong.max")
    @DecimalMin(value = "-180.0", message = "valid.post.geoTagLong.min")
    private Double geoTagLong;
}
