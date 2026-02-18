package com.example.pharmaaggregatorserver.service.temp.seller;

import com.example.pharmaaggregatorserver.entity.temp.seller.TempSellerCoordinator;
import com.example.pharmaaggregatorserver.repository.temp.seller.TempSellerCoordinatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TempSellerCoordinatorService {

    @Autowired
    private TempSellerCoordinatorRepository coordinatorRepository;

    public List<TempSellerCoordinator> getAllCoordinators() {
        return coordinatorRepository.findAll();
    }
}