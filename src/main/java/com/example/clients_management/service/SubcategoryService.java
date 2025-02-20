package com.example.clients_management.service;

import com.example.clients_management.entities.Subcategory;
import com.example.clients_management.repositories.SubcategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubcategoryService {
    @Autowired
    private SubcategoryRepository subcategoryRepository;

    public List<Subcategory> getSubcategoriesByServiceId(Long serviceId) {
        return subcategoryRepository.findByCategoryId(serviceId);
    }
}