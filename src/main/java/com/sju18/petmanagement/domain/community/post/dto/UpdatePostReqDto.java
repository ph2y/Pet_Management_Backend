package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePostReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
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
}
