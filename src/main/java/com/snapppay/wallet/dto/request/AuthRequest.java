package com.snapppay.wallet.dto.request;

import com.snapppay.wallet.util.RegexPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class AuthRequest {
    @NotBlank(message = "user.cell.empty.exception")
    @Pattern(regexp = RegexPatterns.MOBILE_PHONE_NUMBER, message = "user.invalid.phone.number.exception")
    private String phoneNumber;

    @NotBlank(message = "user.empty.password.exception")
    private String password;
}
