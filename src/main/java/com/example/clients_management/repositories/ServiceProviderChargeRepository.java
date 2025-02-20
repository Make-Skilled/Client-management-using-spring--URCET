package com.example.clients_management.repositories;

import com.example.clients_management.entities.ServiceProviderCharge;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.entities.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServiceProviderChargeRepository extends JpaRepository<ServiceProviderCharge, Long> {
    List<ServiceProviderCharge> findByServiceProvider(ServiceProviderDetails serviceProvider);
    List<ServiceProviderCharge> findBySubcategory(Subcategory subcategory);
    ServiceProviderCharge findByServiceProviderAndSubcategory(ServiceProviderDetails serviceProvider, Subcategory subcategory);

    @Query("SELECT DISTINCT sp FROM ServiceProviderDetails sp JOIN ServiceProviderCharge spc ON sp = spc.serviceProvider WHERE spc.subcategory.id = :subcategoryId")
    List<ServiceProviderDetails> findBySubcategoryId(@Param("subcategoryId") Long subcategoryId);

    @Query("SELECT spc FROM ServiceProviderCharge spc WHERE spc.serviceProvider.id = :providerId AND spc.subcategory.id = :subcategoryId")
    ServiceProviderCharge findByServiceProviderIdAndSubcategoryId(@Param("providerId") Long providerId, @Param("subcategoryId") Long subcategoryId);
}
