package com.snapppay.wallet.repository;

import com.snapppay.wallet.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long>  {
    @Query("SELECT w FROM Wallet w JOIN FETCH w.user WHERE w.id = :id")
    Optional<Wallet> findById(Long id);
}
