package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.DosageFormDto;
import com.example.pharmaaggregatorserver.dto.product.PackTypeDto;
import com.example.pharmaaggregatorserver.entity.product.DosageForm;
import com.example.pharmaaggregatorserver.entity.product.PackType;
import com.example.pharmaaggregatorserver.repository.product.DosageFormRepository;
import com.example.pharmaaggregatorserver.repository.product.PackTypeRepository;
import com.example.pharmaaggregatorserver.service.product.DosageFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DosageFormServiceImpl implements DosageFormService {

    private final DosageFormRepository dosageFormRepository;
    private final PackTypeRepository packTypeRepository;

    @Override
    public List<DosageFormDto> getAllDosageForms() {
        List<DosageForm> dosageForms = dosageFormRepository.findAll();

        return dosageForms.stream().map(d -> {
            DosageFormDto dto = new DosageFormDto();
            dto.setDosageId(d.getDosageId());
            dto.setDosageName(d.getDosageName());
            dto.setCategoryId(d.getCategory().getCategoryId());
            dto.setCategoryName(d.getCategory().getCategoryName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DosageFormDto> getDosageFormsByCategoryId(Long categoryId) {
        List<DosageForm> dosageForms = dosageFormRepository.findByCategory_CategoryId(categoryId);
        return dosageForms.stream().map(d -> {
            DosageFormDto dto = new DosageFormDto();
            dto.setDosageId(d.getDosageId());
            dto.setDosageName(d.getDosageName());
            dto.setCategoryId(d.getCategory().getCategoryId());
            dto.setCategoryName(d.getCategory().getCategoryName());
            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<PackTypeDto> getPackTypesByDosageId(Long dosageId) {

        List<PackType> packTypes =
                packTypeRepository.findByDosageForm_DosageId(dosageId);

        return packTypes.stream().map(p -> {
            PackTypeDto dto = new PackTypeDto();
            dto.setPackId(p.getPackId());
            dto.setPackType(p.getPackType());
            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<PackTypeDto> getPackTypesByCategoryId(Long categoryId) {

        List<PackType> packTypes =
                packTypeRepository.findByCategory_CategoryId(categoryId);

        return packTypes.stream().map(p -> {
            PackTypeDto dto = new PackTypeDto();
            dto.setPackId(p.getPackId());
            dto.setPackType(p.getPackType());
            return dto;
        }).collect(Collectors.toList());
    }


}
