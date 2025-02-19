
package com.example.clients_management.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clients_management.entities.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByClientEmail(String clientEmail);
}