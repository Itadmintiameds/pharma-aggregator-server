package com.example.pharmaaggregatorserver.repository.ifsc;

import com.example.pharmaaggregatorserver.entity.ifsc.IFSCOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IFSCOverrideRepository extends JpaRepository<IFSCOverride, String> {
    Optional<IFSCOverride> findByIfscCodeAndIsActiveTrue(String ifscCode);
}