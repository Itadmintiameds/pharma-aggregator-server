package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.SellerTypeResponseDTO;
import com.example.pharmaaggregatorserver.entity.master.SellerTypeMaster;
import com.example.pharmaaggregatorserver.repository.master.SellerTypeMasterRepository;
import com.example.pharmaaggregatorserver.service.master.SellerTypeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerTypeMasterServiceImpl implements SellerTypeMasterService {

    private final SellerTypeMasterRepository sellerTypeMasterRepository;

    private SellerTypeResponseDTO convertToResponseDTO(SellerTypeMaster entity) {
        return new SellerTypeResponseDTO(
                entity.getSellerTypeId(),
                entity.getSellerTypeName(),
                entity.getIsActive()
        );
    }

    @Override
    public List<SellerTypeResponseDTO> getAllSellerTypes() {
        return sellerTypeMasterRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
