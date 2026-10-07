package com.example.pharmaaggregatorserver.service.seller.sellerImpl;

import com.example.pharmaaggregatorserver.dto.seller.SellerResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.mapper.seller.AllSeller.SellerMapper;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;

    @Transactional(readOnly = true)
    public List<SellerResponseDTO> getAllSellers() {
        log.info("Fetching all active sellers");
        List<Seller> sellers = sellerRepository.findAllActiveSellers();
        return sellerMapper.toDTOList(sellers);
    }
}