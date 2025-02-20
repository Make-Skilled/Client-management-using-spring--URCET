package com.example.clients_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.entities.ServiceProviderCharge;
import com.example.clients_management.entities.Subcategory;
import com.example.clients_management.repositories.ServiceProviderChargeRepository;
import com.example.clients_management.repositories.ServiceProviderRepository;
import com.example.clients_management.repositories.SubcategoryRepository;

@Service
public class ServiceProviderChargeService {
    
    @Autowired
    private ServiceProviderChargeRepository serviceProviderChargeRepository;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;

    public List<ServiceProviderDetails> getServiceProvidersBySubcategoryId(Long subcategoryId) {
        return serviceProviderChargeRepository.findBySubcategoryId(subcategoryId);
    }

    public ServiceProviderCharge getChargeByProviderAndSubcategory(Long providerId, Long subcategoryId) {
        ServiceProviderDetails provider = serviceProviderRepository.findById(providerId).orElse(null);
        Subcategory subcategory = subcategoryRepository.findById(subcategoryId).orElse(null);
        
        if (provider != null && subcategory != null) {
            return serviceProviderChargeRepository.findByServiceProviderAndSubcategory(provider, subcategory);
        }
        return null;
    }
}
