package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.StateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateMasterRepository extends JpaRepository<StateMaster, Long> {
}
