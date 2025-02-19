
package com.example.clients_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clients_management.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}