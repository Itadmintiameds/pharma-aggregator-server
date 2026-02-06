package com.example.pharmaaggregatorserver.service.serviceImpl.temp.seller;

import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerAdminResponseDTO;
import com.example.pharmaaggregatorserver.dto.response.temp.seller.TempSellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.temp.seller.TempSeller;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerRepository;
import com.example.pharmaaggregatorserver.service.temp.seller.TempSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempSellerServiceImpl implements TempSellerService {

    private final TempSellerRepository tempSellerRepository;

    /* Get All Temporary Sellers */
    @Override
    public List<TempSellerAdminResponseDTO> getALLTempSellers() {
        List<TempSeller> tempSellers = tempSellerRepository.findAll();

        if (tempSellers.isEmpty()) {
            return List.of();
        }
        List<TempSellerAdminResponseDTO> dtos = new ArrayList<>();
        tempSellers.forEach(tempSeller -> {
            TempSellerAdminResponseDTO dto = new TempSellerAdminResponseDTO();
            dto.setTempSellerId(tempSeller.getTempSellerId());
            dto.setTempSellerRequestId(tempSeller.getTempSellerRequestId());
            dto.setTempSellerName(tempSeller.getSellerName());
            dto.setTempSellerEmail(tempSeller.getEmail());
            dto.setCreatedAt(tempSeller.getCreatedAt());
            dto.setStatus(tempSeller.getStatus());
            dtos.add(dto);
        });
        return dtos;
    }

    /* Get All Temporary Sellers */
    @Override
    public TempSellerResponseDTO findById() {
        return null;
    }
}
