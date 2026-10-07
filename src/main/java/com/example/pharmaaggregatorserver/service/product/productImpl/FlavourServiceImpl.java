package com.example.pharmaaggregatorserver.service.product.productImpl;

import com.example.pharmaaggregatorserver.dto.product.FlavourMasterDto;
import com.example.pharmaaggregatorserver.entity.product.Flavour;
import com.example.pharmaaggregatorserver.exception.NotFoundException;
import com.example.pharmaaggregatorserver.mapper.product.FlavourMapper;
import com.example.pharmaaggregatorserver.repository.product.FlavourRepository;
import com.example.pharmaaggregatorserver.service.product.FlavourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlavourServiceImpl implements FlavourService {

    private final FlavourRepository flavourRepository;
    private final FlavourMapper flavourMapper;

    @Override
    public List<FlavourMasterDto> findAll() {
        List<Flavour> flavours = flavourRepository.findAll();
        if (flavours.isEmpty()) {
            return List.of();
        }
        return flavourMapper.toDtoList(flavours);
    }

    @Override
    public FlavourMasterDto findById(Long id) {
        Flavour flavour = flavourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flavour not found with id: " + id));
        return flavourMapper.toDto(flavour);
    }

    @Override
    public FlavourMasterDto createFlavour(FlavourMasterDto dto) {
        Flavour flavour = flavourMapper.toEntity(dto, new Flavour());
        return flavourMapper.toDto(flavourRepository.save(flavour));
    }

    @Override
    public FlavourMasterDto updateFlavour(Long flavourId, FlavourMasterDto dto) {
        Flavour flavour = flavourRepository.findById(flavourId)
                .orElseThrow(() -> new NotFoundException("Flavour not found with id: " + flavourId));
        flavourMapper.toEntity(dto, flavour);
        return flavourMapper.toDto(flavourRepository.save(flavour));
    }

    @Override
    public String deleteById(Long id) {
        flavourRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flavour not found with id: " + id));
        flavourRepository.deleteById(id);
        return "Flavour has been deleted";
    }
}
