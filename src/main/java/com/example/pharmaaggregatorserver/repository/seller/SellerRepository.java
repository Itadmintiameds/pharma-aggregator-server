package com.example.pharmaaggregatorserver.repository.seller;

import com.example.pharmaaggregatorserver.entity.seller.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, String> {
}
