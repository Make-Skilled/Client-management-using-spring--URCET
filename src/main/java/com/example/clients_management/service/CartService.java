package com.example.clients_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.clients_management.entities.CartItem;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.repositories.CartItemRepository;
import com.example.clients_management.repositories.ServiceRepository;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;

    public void addToCart(ServiceProviderDetails provider, String clientEmail, int quantity) {
        List<com.example.clients_management.entities.Service> services = serviceRepository.findByServiceProvider(provider);
        if (!services.isEmpty()) {
            com.example.clients_management.entities.Service service = services.get(0); // Get the first service
            
            for(int i = 0; i < quantity; i++) {
                CartItem cartItem = new CartItem();
                cartItem.setClientEmail(clientEmail);
                cartItem.setServiceProviderId(provider.getId());
                cartItem.setProviderName(provider.getName());
                cartItem.setProviderPhone(provider.getMobile());
                cartItem.setProviderEmail(provider.getEmail());
                cartItem.setProviderLocation(provider.getLocation());
                cartItem.setPreferredService(provider.getPreferredService());
                cartItem.setService(service);
                cartItemRepository.save(cartItem);
            }
        }
    }

    public List<CartItem> getCartItems(String clientEmail) {
        return cartItemRepository.findByClientEmail(clientEmail);
    }

    public CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}