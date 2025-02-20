package com.example.clients_management.repositories;

import com.example.clients_management.entities.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    List<Subcategory> findByCategoryName(String categoryName);
    List<Subcategory> findByCategoryId(Long categoryId);
    
}
