package com.example.clients_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clients_management.entities.AdminDetails;

public interface AdminRepository extends JpaRepository<AdminDetails, Long> {
    AdminDetails findByEmail(String email);
}