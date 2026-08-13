package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.dto.master.RequestDTO.BuyerTypeMasterDTO;
import com.example.pharmaaggregatorserver.entity.master.BuyerTypeMaster;
import com.example.pharmaaggregatorserver.repository.master.BuyerTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.BuyerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuyerTypeMasterServiceImpl implements BuyerTypeMasterService {

    private final BuyerTypeMasterRepository buyerTypeMasterRepository;

    private BuyerTypeMasterDTO convertToDTO(BuyerTypeMaster entity) {
        return new BuyerTypeMasterDTO(
                entity.getBuyerTypeId(),
                entity.getBuyerTypeName(),
                entity.getBuyerTypeAbbreviation(),
                entity.getMandatoryDocumentTypeId() != null ? entity.getMandatoryDocumentTypeId().getDocumentTypeId() : null,
                entity.getIsActive()
        );
    }

    @Override
    public List<BuyerTypeMasterDTO> findAllActive() {
        return buyerTypeMasterRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
