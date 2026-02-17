package com.example.pharmaaggregatorserver.service.serviceImpl.master;

import com.example.pharmaaggregatorserver.entity.master.TalukaMaster;
import com.example.pharmaaggregatorserver.repository.temp.master.TalukaMasterRepository;
import com.example.pharmaaggregatorserver.service.master.TalukaMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalukaMasterServiceImpl implements TalukaMasterService {

    private final TalukaMasterRepository talukaMasterRepository;

    // Get all talukas
    @Override
    public List<TalukaMaster> getAllTalukas() {
        List<TalukaMaster> talukas = talukaMasterRepository.findAll();
        if (talukas.isEmpty()) {
            return List.of();
        }
        return talukas;
    }

    // Get talukas by district ID
    @Override
    public List<TalukaMaster> getTalukasByDistrictId(Integer districtId) {
        List<TalukaMaster> talukas = talukaMasterRepository.findByDistrict_DistrictId(districtId);
        if (talukas.isEmpty()) {
            return List.of();
        }
        return talukas;
    }
}