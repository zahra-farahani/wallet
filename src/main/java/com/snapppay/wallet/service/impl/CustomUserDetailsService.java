package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.entity.Role;
import com.snapppay.wallet.exception.messages.UserNotFoundException;
import com.snapppay.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByPhoneNumber(username)
                .orElseThrow(UserNotFoundException::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getPhoneNumber())
                .password(u.getPasswordHash())
                .disabled(!u.getActive())
                .roles(extractRoles(u))
                .build();
    }

    private String[] extractRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getType)
                .map(Enum::name)
                .distinct()
                .toArray(String[]::new);
    }

}
