package com.example.pharmaaggregatorserver.repository.temp.buyer;

import com.example.pharmaaggregatorserver.entity.temp.buyer.TempBuyerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempBuyerContactRepository extends JpaRepository<TempBuyerContact, Long> {
    boolean existsByEmail(String email);

    // Excludes the caller's own in-progress draft so re-visiting the contact
    // step of their own draft doesn't flag their own previously-saved email
    // as a duplicate. Mirrors TempSellerCoordinatorRepository.
    boolean existsByEmailAndBuyer_TempBuyerIdNot(String email, Long tempBuyerId);

    boolean existsByMobile(String mobile);

    boolean existsByMobileAndBuyer_TempBuyerIdNot(String mobile, Long tempBuyerId);

    Optional<TempBuyerContact> findByBuyer_TempBuyerId(Long tempBuyerId);
}
