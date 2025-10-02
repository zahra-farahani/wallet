package com.snapppay.wallet.exception.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    @JsonProperty("api_status")
    private String apiStatus;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("message")
    private String message;
}
