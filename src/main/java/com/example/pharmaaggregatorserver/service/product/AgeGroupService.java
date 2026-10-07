package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.AgeGroupMasterDto;

import java.util.List;

public interface AgeGroupService {

    List<AgeGroupMasterDto> getAllAgeGroups();

}
