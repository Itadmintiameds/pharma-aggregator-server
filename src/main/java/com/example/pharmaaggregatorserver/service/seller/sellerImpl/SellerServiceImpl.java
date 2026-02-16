package com.example.pharmaaggregatorserver.service.seller.sellerImpl;

import com.example.pharmaaggregatorserver.dto.seller.SellerDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.repository.seller.SellerBankDetailsRepository;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.service.seller.SellerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final SellerBankDetailsRepository sellerBankDetailsRepository;

    @Override
    public List<SellerDTO> findAll() {
        List<Seller> sellers = sellerRepository.findAll();
        if (sellers.isEmpty()) {
            return List.of();
        }
        return sellers;
    }

    @Override
    public SellerDTO findBySellerId(String sellerId) {
        return null;
    }

    @Override
    public SellerDTO save(SellerDTO sellerDTO) {
        return null;
    }

    @Override
    public SellerDTO deleteBySellerId(String sellerId) {
        return null;
    }

    @Override
    public SellerDTO updateBySellerId(String sellerId, SellerDTO sellerDTO) {
        return null;
    }
}
