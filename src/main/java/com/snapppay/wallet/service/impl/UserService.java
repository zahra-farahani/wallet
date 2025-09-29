package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.Mapper.UserMapper;
import com.snapppay.wallet.dto.request.UserRegisterRequest;
import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.repository.UserRepository;
import com.snapppay.wallet.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public void register(UserRegisterRequest request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new IllegalArgumentException("username taken");
        }
        User user = mapper.mapRequestToEntity(request);
        userRepository.save(user);
    }
}
