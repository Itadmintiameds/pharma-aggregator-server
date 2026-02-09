package com.example.pharmaaggregatorserver.service.master;
import com.example.pharmaaggregatorserver.dto.master.ResponseDTO.TalukaResponseDTO;
import java.util.List;

public interface TalukaMasterService {
    List<TalukaResponseDTO> getAllTalukas();
}
