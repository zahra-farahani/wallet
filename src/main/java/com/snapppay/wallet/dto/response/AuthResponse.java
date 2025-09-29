package com.snapppay.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(exclude = "token")
public class AuthResponse {
    private String token;
}
