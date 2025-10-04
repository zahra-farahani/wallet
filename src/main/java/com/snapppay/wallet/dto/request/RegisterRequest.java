package com.snapppay.wallet.dto.request;

import com.snapppay.wallet.util.RegexPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class RegisterRequest {
    @NotBlank(message = "user.cell.empty.exception")
    @Pattern(regexp = RegexPatterns.MOBILE_PHONE_NUMBER, message = "user.invalid.phone.number.exception")
    private String phoneNumber;

    @NotBlank(message = "user.firstname.empty.exception")
    private String firstName;

    @NotBlank(message = "user.lastname.empty.exception")
    private String lastName;

    @NotBlank(message = "user.empty.password.exception")
    @Size(min = 8, message = "user.invalid.password.exception")
    private String password;
}
