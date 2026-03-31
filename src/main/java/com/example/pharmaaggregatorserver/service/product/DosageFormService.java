package com.example.pharmaaggregatorserver.service.product;

import com.example.pharmaaggregatorserver.dto.product.DosageFormDto;
import com.example.pharmaaggregatorserver.dto.product.PackTypeDto;

import java.util.List;


public interface DosageFormService {

    List<DosageFormDto> getAllDosageForms();

    List<PackTypeDto> getPackTypesByDosageId(Long dosageId);


}
