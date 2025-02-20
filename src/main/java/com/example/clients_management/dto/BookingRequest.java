package com.example.clients_management.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequest {
    private Long providerId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;

    // Getters and Setters
    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
