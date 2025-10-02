package com.snapppay.wallet.service;

import com.snapppay.wallet.dto.request.RegisterRequest;

public interface IUserService {
    void register(RegisterRequest request);
}
