package com.snapppay.wallet.Mapper;

import com.snapppay.wallet.dto.request.RegisterRequest;
import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.security.PasswordUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = PasswordUtil.class)
public interface UserMapper {
    @Mapping(target = "passwordHash", expression = "java(PasswordUtil.hashPassword(request.getPassword()))")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", constant = "true")
    User mapRegisterRequestToEntity(RegisterRequest request);
}
