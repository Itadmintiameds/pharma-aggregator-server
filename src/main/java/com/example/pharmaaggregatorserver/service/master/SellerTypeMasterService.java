package com.example.pharmaaggregatorserver.service.master;
import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.SellerTypeResponseDTO;
import java.util.List;
public interface SellerTypeMasterService {
    List<SellerTypeResponseDTO> getAllSellerTypes();
}
