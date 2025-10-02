package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.Mapper.UserMapper;
import com.snapppay.wallet.dto.request.RegisterRequest;
import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.exception.messages.AlreadyExistedPhoneNumberException;
import com.snapppay.wallet.repository.UserRepository;
import com.snapppay.wallet.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new AlreadyExistedPhoneNumberException();
        }
        User user = mapper.mapRegisterRequestToEntity(request);
        userRepository.save(user);
    }
}
