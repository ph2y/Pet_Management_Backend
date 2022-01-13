package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateBookmarkReqDto {
    @PositiveOrZero(message = "valid.place.id.notNegative")
    private Long placeId;
    @NotBlank(message = "valid.bookmark.name.blank")
    @Size(max = 20, message = "valid.bookmark.name.size")
    private String name;
    @Size(max = 250, message = "valid.bookmark.description.size")
    private String description;
    @Size(max = 20, message = "valid.bookmark.folder.name.size")
    private String folder;
}
