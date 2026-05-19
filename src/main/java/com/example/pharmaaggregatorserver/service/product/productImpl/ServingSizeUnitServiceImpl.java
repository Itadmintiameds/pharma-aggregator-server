package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.ServingSizeUnitDto;
import com.example.pharmaaggregatorserver.entity.product.ServingSizeUnit;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.mapper.product.ServingSizeUnitMapper;
import com.example.pharmaaggregatorserver.repository.product.ServingSizeUnitRepository;
import com.example.pharmaaggregatorserver.service.product.ServingSizeUnitService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ServingSizeUnitServiceImpl implements ServingSizeUnitService {

    private final ServingSizeUnitRepository servingSizeUnitRepository;
    private final ServingSizeUnitMapper servingSizeUnitMapper;

    @Override
    public List<ServingSizeUnitDto> findAll() {
        List<ServingSizeUnit> servingSizeUnits = servingSizeUnitRepository.findAll();

        if (servingSizeUnits.isEmpty()) {
            return List.of();
        }

        return servingSizeUnits
                .stream()
                .map(servingSizeUnitMapper::toDto)
                .toList();
    }

    @Override
    public ServingSizeUnitDto findById(Long id) {
        ServingSizeUnit servingSizeUnit = servingSizeUnitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServingSize not found with id " + id));
        return servingSizeUnitMapper.toDto(servingSizeUnit);
    }

    @Override
    public List<ServingSizeUnitDto> findByDosageFormId(Long id) {
        List<ServingSizeUnit> servingSizeUnits = servingSizeUnitRepository.findByDosageForm_DosageId(id);

        if (servingSizeUnits.isEmpty()) {
            return List.of();
        }

        return servingSizeUnits
                .stream()
                .map(servingSizeUnitMapper::toDto)
                .toList();
    }

    @Override
    public List<ServingSizeUnitDto> findByProductFormId(Long id) {
        List<ServingSizeUnit> servingSizeUnits = servingSizeUnitRepository.findByProductForm_ProductFormId(id);

        if (servingSizeUnits.isEmpty()) {
            return List.of();
        }

        return servingSizeUnits
                .stream()
                .map(servingSizeUnitMapper::toDto)
                .toList();
    }
}
