package com.snapppay.wallet.service;

import com.snapppay.wallet.dto.request.AuthRequest;
import com.snapppay.wallet.dto.request.RegisterRequest;
import com.snapppay.wallet.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse login(AuthRequest request);
    void register(RegisterRequest request);
}
