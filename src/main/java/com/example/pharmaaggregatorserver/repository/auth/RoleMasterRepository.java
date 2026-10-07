package com.example.pharmaaggregatorserver.repository.auth;

import com.example.pharmaaggregatorserver.entity.master.RoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleMasterRepository extends JpaRepository<RoleMaster, Integer> {
    Optional<RoleMaster> findByRoleName(String roleName);
}