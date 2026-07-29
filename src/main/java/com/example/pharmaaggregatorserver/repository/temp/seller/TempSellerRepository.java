package com.example.pharmaaggregatorserver.repository.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
public interface TempSellerRepository extends JpaRepository<TempSeller, Long> {
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByGstNumber(String gstnumber);

    Optional<TempSeller> findByUser_UserId(Long userId);

    @Query("SELECT MAX(s.tempSellerRequestId) FROM TempSeller s")
    Optional<String> findMaxRequestId();
}