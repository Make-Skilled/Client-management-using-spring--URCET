package com.example.clients_management.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.clients_management.entities.Bookings;
import com.example.clients_management.repositories.BookingsRepository;

@Service
public class BookingService {

    @Autowired
    private BookingsRepository bookingRepository;

    public boolean cancelBooking(Long id) {
        Optional<Bookings> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Bookings booking = bookingOptional.get();
            booking.setStatus("Canceled");
            bookingRepository.save(booking);
            return true;
        }
        return false;
    }
    
    public boolean updateBookingStatus(Long id, String status) {
        Optional<Bookings> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Bookings booking = bookingOptional.get();
            booking.setStatus(status); // Update the status
            bookingRepository.save(booking); // Save to database
            return true;
        }
        return false; // Booking not found
    }
}
