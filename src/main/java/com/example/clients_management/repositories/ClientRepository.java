package com.example.clients_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clients_management.entities.ClientDetails;

public interface ClientRepository extends JpaRepository<ClientDetails,Long> {
	
	ClientDetails findByEmail(String email);
    boolean existsByName(String username);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
}
