package com.example.clients_management.repositories;

import com.example.clients_management.entities.ServiceProviderCharge;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.entities.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceProviderChargeRepository extends JpaRepository<ServiceProviderCharge, Long> {
    List<ServiceProviderCharge> findByServiceProvider(ServiceProviderDetails serviceProvider);
    ServiceProviderCharge findByServiceProviderAndSubcategory(ServiceProviderDetails serviceProvider, Subcategory subcategory);
}
