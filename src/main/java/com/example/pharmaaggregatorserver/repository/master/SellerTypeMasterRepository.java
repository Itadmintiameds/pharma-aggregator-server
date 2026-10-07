package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerTypeMasterRepository extends JpaRepository<SellerTypeMaster, Long> {
}
