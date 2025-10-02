package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.dto.request.AuthRequest;
import com.snapppay.wallet.dto.request.RegisterRequest;
import com.snapppay.wallet.dto.response.AuthResponse;
import com.snapppay.wallet.entity.User;
import com.snapppay.wallet.exception.messages.UserCredentialsException;
import com.snapppay.wallet.exception.messages.UserNotFoundException;
import com.snapppay.wallet.repository.UserRepository;
import com.snapppay.wallet.security.jwt.JwtUtil;
import com.snapppay.wallet.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            // Get user details
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(UserNotFoundException::new);

            // Authenticate user
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhoneNumber(),
                            request.getPassword())
            );

            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getPhoneNumber(),
                    user.getId(),
                    user.getRoles()
            );

            return new AuthResponse(token);

        } catch (BadCredentialsException e) {
            throw new UserCredentialsException();
        }
    }

    @Override
    public void register(RegisterRequest request) {
        userService.register(request);
    }

}
