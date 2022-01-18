package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class UpdateFcmRegistrationTokenReqDto {
    private String fcmRegistrationToken;
}
