package com.example.clients_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clients_management.entities.Service;
import com.example.clients_management.entities.ServiceProviderDetails;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByServiceProvider(ServiceProviderDetails serviceProvider);

    List<Service> findByCategory_Id(Long categoryId);
}
