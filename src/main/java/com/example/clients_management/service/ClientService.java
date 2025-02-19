package com.example.clients_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.clients_management.repositories.ClientRepository;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;
    
    // Add your service methods here

    public boolean existsByMobile(String mobile) {
        return clientRepository.existsByMobile(mobile);
    }
}