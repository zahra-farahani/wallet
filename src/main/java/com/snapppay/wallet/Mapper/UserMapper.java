package com.snapppay.wallet.Mapper;

import com.snapppay.wallet.dto.request.UserRegisterRequest;
import com.snapppay.wallet.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User mapRequestToEntity(UserRegisterRequest request);
}
