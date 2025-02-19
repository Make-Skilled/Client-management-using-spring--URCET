package com.example.clients_management.controllers;

import java.util.List;

import com.example.clients_management.entities.Category;
import com.example.clients_management.entities.Service;
import com.example.clients_management.entities.Bookings;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.repositories.CategoryRepository;
import com.example.clients_management.repositories.ServiceProviderRepository;
import com.example.clients_management.repositories.ServiceRepository;
import com.example.clients_management.repositories.BookingsRepository;
import com.example.clients_management.service.BookingService;
import com.example.clients_management.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class ServiceProviderController {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    BookingsRepository bookingsRepository;
    
    @Autowired
    BookingService bookingService;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
        
    private final PasswordEncoder passwordEncoder;

    public ServiceProviderController(ServiceProviderRepository serviceProviderRepository, PasswordEncoder passwordEncoder) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/serviceproviderlogin")
    public String login() {
        return "serviceProviderLogin";
    }
    
    @GetMapping("/serviceprovidersignup")
    public String serviceProviderSignup(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "serviceProviderRegister";
    }

    @PostMapping("/serviceprovidersignup")
    public String registerServiceProvider(ServiceProviderDetails serviceProvider, 
                                   @RequestParam(value = "socialService", defaultValue = "0") String socialService,
                                   Model model) {
    
        // Check if the username, email, or mobile number already exists
        if (serviceProviderRepository.existsByName(serviceProvider.getName())) {
            model.addAttribute("error", "Username is already taken.");
            return "serviceProviderRegister";
        }

        if (serviceProviderRepository.existsByEmail(serviceProvider.getEmail())) {
            model.addAttribute("error", "Email is already registered.");
            return "serviceProviderRegister";
        }

        if (serviceProviderRepository.existsByMobile(serviceProvider.getMobile())) {
            model.addAttribute("error", "Mobile number is already registered.");
            return "serviceProviderRegister";
        }

        if (serviceProvider.getMobile().length() < 10) {
            model.addAttribute("error", "Mobile number must be at least 10 digits.");
            return "serviceProviderRegister";
        }

        // Handle social service case
        serviceProvider.setSocialService(Integer.valueOf(socialService));
        

        // Encode the password using BCryptPasswordEncoder
        serviceProvider.setPassword(passwordEncoder.encode(serviceProvider.getPassword()));

        // Save the new service provider details
        serviceProviderRepository.save(serviceProvider);
        model.addAttribute("success", "Registration successful!");
        return "serviceProviderLogin";
    }
    
    @PostMapping("/serviceproviderlogin")
    public String loginServiceProvider(String email, String password, Model model, HttpSession session) {
        ServiceProviderDetails serviceProvider = serviceProviderRepository.findByEmail(email);
        if (serviceProvider == null) {
            model.addAttribute("errorMessage", "Email not found");
            return "serviceProviderLogin";
        }
        if (!passwordEncoder.matches(password, serviceProvider.getPassword())) {
            model.addAttribute("errorMessage", "Password is wrong");
            return "serviceProviderLogin";
        }
        session.setAttribute("serviceProviderEmail", serviceProvider.getEmail());
        session.setAttribute("serviceProviderId", serviceProvider.getId());
        session.setAttribute("userType", "serviceprovider");
        return "redirect:/serviceproviderdashboard";
    }

    @GetMapping("/serviceproviderlogout")
    public String logout(HttpSession session) {
        // Invalidate the session to log out the user
        session.invalidate();
        return "redirect:/serviceproviderlogin"; // Redirect to the login page after logout
    }
    
    
    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model) {
        String email = (String) session.getAttribute("serviceProviderEmail"); // Assuming you store the email in the session
        if (email == null) {
            return "redirect:/serviceproviderlogin"; // Redirect to login if no email is found
        }
        
        // Fetch the service provider details
        ServiceProviderDetails serviceProvider = serviceProviderRepository.findByEmail(email);
        if (serviceProvider != null) {
            model.addAttribute("serviceProvider", serviceProvider);
        } else {
            model.addAttribute("errorMessage", "Service Provider not found.");
        }
        
        return "profile"; // This should match the name of your Thymeleaf template
    }
    
    @GetMapping("/serviceproviderdashboard")
    public String dash(HttpSession session, Model model) {
        String email = (String) session.getAttribute("serviceProviderEmail");
        if (email == null) {
            return "redirect:/serviceproviderlogin";
        }
        ServiceProviderDetails serviceProvider = serviceProviderRepository.findByEmail(email);
        if (serviceProvider != null) {
            model.addAttribute("serviceProvider", serviceProvider);
            model.addAttribute("services", serviceRepository.findByServiceProvider(serviceProvider));
        } else {
            model.addAttribute("errorMessage", "Service Provider not found.");
        }
        return "serviceProviderDashboard";
    }
    
    @PostMapping("/updateprofile")
    public String updateProfile(ServiceProviderDetails updatedServiceProvider, RedirectAttributes redirectAttributes) {
        // Fetch the existing service provider
        ServiceProviderDetails existingServiceProvider = serviceProviderRepository.findById(updatedServiceProvider.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid service provider ID: " + updatedServiceProvider.getId()));

        // Update the existing service provider's details
        existingServiceProvider.setName(updatedServiceProvider.getName());
        existingServiceProvider.setMobile(updatedServiceProvider.getMobile());
        existingServiceProvider.setEmail(updatedServiceProvider.getEmail());
        existingServiceProvider.setLocation(updatedServiceProvider.getLocation());
        existingServiceProvider.setPreferredService(updatedServiceProvider.getPreferredService());

        // Save the updated service provider
        serviceProviderRepository.save(existingServiceProvider);

        // Add success message to redirect attributes
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        // Redirect to the profile page
        return "redirect:/profile?id=" + updatedServiceProvider.getId();
    }

    
    @GetMapping("/editprofile")
    public String getServiceProviderDetails(@RequestParam("id") Long id, Model model) {
        // Fetch the service provider details from the database
        ServiceProviderDetails serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service provider ID: " + id));

        // Add the service provider details to the model
        model.addAttribute("serviceProvider", serviceProvider);

        // Return the edit profile view
        return "editprofile"; // Name of your Thymeleaf template
    }
    
    @GetMapping("/getserviceproviders")
    public String getServiceProviders(@RequestParam("filter") String filter, Model model) {
        // Fetch service providers based on the filter
        List<ServiceProviderDetails> serviceProviders = serviceProviderRepository.findByPreferredService(filter);
        
        // Add the result to the model to pass it to the view
        model.addAttribute("serviceProviders", serviceProviders);
        
        // Return the view name where the service providers will be displayed
        return "serviceProviders"; // Make sure you have this template to display the list
    }
    

    @GetMapping("/mybookings")
    public String myBookings(HttpSession session, Model model) {
        // Check if the userType exists in the session
        String userType = (String) session.getAttribute("userType");

        // If userType is null, redirect to login page
        if (userType == null) {
            return "redirect:/login"; // Redirect to the login page
        }

        // Retrieve the serviceProviderId from the session
        Long serviceProviderId = (Long) session.getAttribute("serviceProviderId");

        // If the serviceProviderId is not found, throw an exception
        if (serviceProviderId == null) {
            throw new RuntimeException("Service Provider ID not found in session.");
        }

        // Fetch bookings with status "Pending" and the specified serviceProviderId
        List<Bookings> bookings = bookingsRepository.findByServiceProviderIdAndStatus(serviceProviderId, "Pending");

        // Add the bookings and userType to the model
        model.addAttribute("bookings", bookings);
        model.addAttribute("userType", userType);

        // Return the view name to render
        return "mybookings"; // Renders the mybookings view
    }

    
    @GetMapping("/rejectbooking/{id}")
    public String rejectBooking(@PathVariable Long id, Model model) {
        try {
            boolean updated = bookingService.updateBookingStatus(id, "REJECTED");
            if (updated) {
                model.addAttribute("message", "Booking rejected successfully.");
            } else {
                model.addAttribute("error", "Booking not found.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error rejecting booking: " + e.getMessage());
        }
        List<Bookings> bookings = bookingsRepository.findByServiceProviderIdAndStatus(id, "Pending");
        model.addAttribute("bookings", bookings);
        return "mybookings"; // Return the mybookings.html view
    }

    @GetMapping("/acceptbooking/{id}")
    public String acceptBooking(@PathVariable Long id, Model model) {
        try {
            boolean updated = bookingService.updateBookingStatus(id, "ACCEPTED");
            if (updated) {
                model.addAttribute("message", "Booking accepted successfully.");
            } else {
                model.addAttribute("error", "Booking not found.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error accepting booking: " + e.getMessage());
        }
        List<Bookings> bookings = bookingsRepository.findByServiceProviderIdAndStatus(id, "Pending");
        model.addAttribute("bookings", bookings);
        return "mybookings"; // Return the mybookings.html view
    }
    
    @GetMapping("/acceptedbookings")
    public String accepted(Model model,HttpSession session) {
    	System.out.print(session.getAttribute("serviceProviderId"));
    	List<Bookings> bookings=bookingsRepository.findByServiceProviderIdAndStatus((Long)session.getAttribute("serviceProviderId"), "ACCEPTED");
    	model.addAttribute("bookings",bookings);
    	return "acceptedbookings";
    }
    
    @GetMapping("/finishbooking/{id}")
    public String finishBooking(@PathVariable Long id, Model model) {
        try {
            boolean updated = bookingService.updateBookingStatus(id, "FINISHED");
            if (updated) {
                model.addAttribute("message", "Booking marked as finished successfully.");
            } else {
                model.addAttribute("error", "Booking not found.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error updating booking: " + e.getMessage());
        }
        return "redirect:/acceptedbookings";
    }
    
    @GetMapping("/addservice")
    public String showAddServicePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("serviceProviderEmail");
        if (email == null) {
            return "redirect:/serviceproviderlogin";
        }
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "addservice";
    }

    @PostMapping("/addservice")
    public String addService(
            @RequestParam String serviceName,
            @RequestParam Double chargePerHour,
            @RequestParam Long categoryId,
            HttpSession session,
            Model model) {
        String email = (String) session.getAttribute("serviceProviderEmail");
        if (email == null) {
            return "redirect:/serviceproviderlogin";
        }

        ServiceProviderDetails serviceProvider = serviceProviderRepository.findByEmail(email);
        if (serviceProvider == null) {
            return "redirect:/serviceproviderlogin";
        }

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Service service = new Service();
        service.setServiceName(serviceName);
        service.setChargePerHour(chargePerHour);
        service.setServiceProvider(serviceProvider);
        service.setCategory(category);

        serviceRepository.save(service);
        return "redirect:/serviceproviderdashboard";
    }
}
