package com.example.pharmaaggregatorserver.repository;

import com.example.pharmaaggregatorserver.entity.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<StateEntity, Long> {
}
