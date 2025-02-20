package com.example.clients_management.services;

import org.springframework.stereotype.Service;

@Service
public class QRCodeService {
    
    public String generatePaymentUrl(Long bookingId) {
        // Generate a URL that points to our test payment endpoint
        return "http://localhost:8080/test-payment/" + bookingId;
    }
}
