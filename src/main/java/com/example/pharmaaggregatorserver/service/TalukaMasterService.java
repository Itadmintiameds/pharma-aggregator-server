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
        return talukaMasterRepository.findAll();
    }

    // Get talukas by district ID
    public List<TalukaMaster> getTalukasByDistrictId(Integer districtId) {
        return talukaMasterRepository.findByDistrictId(districtId);
    }
}