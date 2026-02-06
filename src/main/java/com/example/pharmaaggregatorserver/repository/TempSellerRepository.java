package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.temp.TempSeller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempSellerRepository extends JpaRepository<TempSeller, Long> {
}
