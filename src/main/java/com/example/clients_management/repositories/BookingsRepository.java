package com.example.clients_management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clients_management.entities.Bookings;

public interface BookingsRepository extends JpaRepository<Bookings,Long> {
	List<Bookings> findByServiceProviderId(long serviceProviderId);	
	List<Bookings> findByServiceProviderIdAndStatus(long serviceProviderId, String status);
	List<Bookings> findByBookedBy(String clientEmail);
	List<Bookings> findByServiceProviderIdAndStatusIn(long serviceProviderId, List<String> statuses);
}
