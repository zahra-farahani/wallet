package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.dto.request.AuthRequest;
import com.snapppay.wallet.dto.response.AuthResponse;
import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.exception.messages.UserCredentialsException;
import com.snapppay.wallet.exception.messages.UserNotFoundException;
import com.snapppay.wallet.repository.UserRepository;
import com.snapppay.wallet.security.jwt.JwtUtil;
import com.snapppay.wallet.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(UserNotFoundException::new);

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhoneNumber(),
                            request.getPassword())
            );

            String token = jwtUtil.generateToken(
                    user.getPhoneNumber(),
                    user.getId(),
                    user.getRoles()
            );

            return new AuthResponse(token);

        } catch (BadCredentialsException e) {
            log.debug("BAdCredential for user : {}", request);
            throw new UserCredentialsException();
        }
    }

}
