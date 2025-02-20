package com.example.clients_management.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "service_provider_charges")
public class ServiceProviderCharge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProviderDetails serviceProvider;

    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @Column(nullable = false)
    private Double chargePerHour;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceProviderDetails getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProviderDetails serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public Double getChargePerHour() {
        return chargePerHour;
    }

    public void setChargePerHour(Double chargePerHour) {
        this.chargePerHour = chargePerHour;
    }
}
