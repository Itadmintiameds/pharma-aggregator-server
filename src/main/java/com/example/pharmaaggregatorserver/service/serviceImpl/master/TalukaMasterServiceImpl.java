package com.example.pharmaaggregatorserver.service.serviceImpl.master;



import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.TalukaResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.example.pharmaaggregatorserver.repository.master.TalukaMasterRepository;
import com.example.pharmaaggregatorserver.service.master.TalukaMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalukaMasterServiceImpl implements TalukaMasterService {

    private final TalukaMasterRepository talukaMasterRepository;

    private TalukaResponseDTO convertToResponseDTO(TalukaMaster entity) {
        return new TalukaResponseDTO(
                entity.getTalukaId(),
                entity.getState() != null ? entity.getState().getStateId() : null,
                entity.getState() != null ? entity.getState().getStateName() : null,
                entity.getDistrict() != null ? entity.getDistrict().getDistrictId() : null,
                entity.getDistrict() != null ? entity.getDistrict().getDistrictName() : null,
                entity.getTalukaCode(),
                entity.getTalukaName(),
                entity.getIsActive()
        );
    }

    @Override
    public List<TalukaResponseDTO> getAllTalukas() {
        return talukaMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TalukaResponseDTO> getTalukasByDistrictId(Long districtId) {
        return talukaMasterRepository.findByDistrictDistrictId(districtId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}