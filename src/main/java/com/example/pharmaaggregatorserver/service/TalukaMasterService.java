package com.example.pharmaaggregatorserver.service;

import com.example.pharmaaggregatorserver.entity.TalukaMaster;
import com.example.pharmaaggregatorserver.repository.TalukaMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalukaMasterService {

    private final TalukaMasterRepository talukaMasterRepository;

    // Get all talukas
    public List<TalukaMaster> getAllTalukas() {
        List<TalukaMaster> talukas = talukaMasterRepository.findAll();
        if (talukas.isEmpty()) {
            return List.of();
        }
        return talukas;
    }

    // Get talukas by district ID
    public List<TalukaMaster> getTalukasByDistrictId(Integer districtId) {
        List<TalukaMaster> talukas = talukaMasterRepository.findByDistrictId(districtId);
        if (talukas.isEmpty()) {
            return List.of();
        }
        return talukas;
    }
}