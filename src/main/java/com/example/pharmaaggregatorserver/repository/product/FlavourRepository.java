package com.example.pharmaaggregatorserver.repository.product;

import com.example.pharmaaggregatorserver.entity.product.Flavour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlavourRepository extends JpaRepository<Flavour, Long> {

    Optional<Flavour> findByFlavourName(String flavourName);
}
