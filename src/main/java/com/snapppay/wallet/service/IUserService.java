package com.snapppay.wallet.service;

import com.snapppay.wallet.dto.request.UserRegisterRequest;

public interface IUserService {
    void register(UserRegisterRequest request);
}
