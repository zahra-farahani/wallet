package com.snapppay.wallet.service;

import com.snapppay.wallet.dto.request.RegisterRequest;

import java.util.Optional;

public interface IUserService {
    void register(RegisterRequest request);
}
