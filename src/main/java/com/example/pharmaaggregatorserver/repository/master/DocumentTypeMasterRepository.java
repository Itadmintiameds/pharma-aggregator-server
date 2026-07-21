package com.example.pharmaaggregatorserver.repository.master;

import com.example.pharmaaggregatorserver.entity.master.DocumentTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeMasterRepository extends JpaRepository<DocumentTypeMaster, Long> {
}
