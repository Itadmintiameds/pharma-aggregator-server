package com.example.pharmaaggregatorserver.service.master;

import com.example.pharmaaggregatorserver.dto.master.RequestDTO.BuyerTypeMasterDTO;

import java.util.List;

public interface BuyerTypeMasterService {
    List<BuyerTypeMasterDTO> findAllActive();
}
