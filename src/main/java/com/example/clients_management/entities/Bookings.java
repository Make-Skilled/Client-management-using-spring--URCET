package com.example.clients_management.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Bookings {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serviceProviderId;
	
	@Column
	private String bookedBy;
	
	@Column
	private String date;
	
	@Column
	private String time;
	
	@Column
	private String address;
	
	@Column
	private String phone;
	
	@Column
	private String status;
	
	@Transient // This field won't be persisted in the database
	private ServiceProviderDetails serviceProvider;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getServiceProviderId() {
		return serviceProviderId;
	}

	public void setServiceProviderId(long serviceProviderId) {
		this.serviceProviderId = serviceProviderId;
	}

	public String getBookedBy() {
		return bookedBy;
	}

	public void setBookedBy(String bookedBy) {
		this.bookedBy = bookedBy;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ServiceProviderDetails getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProviderDetails serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
}
