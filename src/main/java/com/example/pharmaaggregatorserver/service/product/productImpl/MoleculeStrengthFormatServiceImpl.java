package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.MoleculeStrengthFormatDto;
import com.example.pharmaaggregatorserver.entity.product.MoleculeStrengthFormat;
import com.example.pharmaaggregatorserver.repository.product.MoleculeStrengthFormatRepository;
import com.example.pharmaaggregatorserver.service.product.MoleculeStrengthFormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoleculeStrengthFormatServiceImpl
        implements MoleculeStrengthFormatService {

    private final MoleculeStrengthFormatRepository repository;

    @Override
    public List<MoleculeStrengthFormatDto> getByDosageId(Long dosageId) {

        List<MoleculeStrengthFormat> entities =
                repository.findByDosageForm_DosageId(dosageId);

        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private MoleculeStrengthFormatDto mapToDto(MoleculeStrengthFormat entity) {
        MoleculeStrengthFormatDto dto = new MoleculeStrengthFormatDto();
        dto.setMoleculeStrengthId(entity.getMoleculeStrengthId());
        dto.setMoleculeStrengthFormat(entity.getMoleculeStrengthFormat());
        return dto;
    }
}
