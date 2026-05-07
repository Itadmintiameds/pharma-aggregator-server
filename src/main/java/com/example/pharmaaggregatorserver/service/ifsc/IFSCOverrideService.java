package com.example.pharmaaggregatorserver.service.ifsc;

import com.example.pharmaaggregatorserver.entity.ifsc.IFSCOverride;
import com.example.pharmaaggregatorserver.repository.ifsc.IFSCOverrideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IFSCOverrideService {

    private final IFSCOverrideRepository ifscOverrideRepository;

    public Map<String, Object> getOverrideByIFSC(String ifscCode) {
        IFSCOverride override = ifscOverrideRepository
                .findByIfscCodeAndIsActiveTrue(ifscCode)
                .orElse(null);

        if (override == null) {
            return null;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("BANK", override.getBank());
        response.put("BRANCH", override.getBranch());
        response.put("STATE", override.getState());
        response.put("DISTRICT", override.getDistrict());
        response.put("CITY", override.getCity());

        if (override.getAddress() != null) {
            response.put("ADDRESS", override.getAddress());
        }

        return response;
    }
}