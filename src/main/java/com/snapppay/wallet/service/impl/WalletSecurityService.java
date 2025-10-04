package com.snapppay.wallet.service.impl;

import com.snapppay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletSecurityService {

    private final WalletRepository walletRepository;

    public boolean canAccessWallet(Long walletId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();

        // Admin can access all
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Check if wallet belongs to user
        return walletRepository.findById(walletId)
                .map(wallet -> wallet.getUser().getPhoneNumber().equals(username))
                .orElse(false);
    }
}
