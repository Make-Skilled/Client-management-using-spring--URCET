package com.example.clients_management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clients_management.entities.ServiceProviderDetails;

public interface ServiceProviderRepository extends JpaRepository<ServiceProviderDetails,Long> {
    boolean existsByName(String username);
    boolean existsByEmail(String email);
	ServiceProviderDetails findByEmail(String email);
	List<ServiceProviderDetails> findByPreferredService(String preferredService);
    boolean existsByMobile(String mobile);
    List<ServiceProviderDetails> findBySubcategoryId(Long subcategoryId);
}
