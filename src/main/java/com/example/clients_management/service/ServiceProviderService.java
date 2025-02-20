package com.example.clients_management.service;

import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.repositories.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServiceProviderService {
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    public List<ServiceProviderDetails> getProvidersBySubcategoryId(Long subcategoryId) {
        return serviceProviderRepository.findBySubcategoryId(subcategoryId);
    }
}