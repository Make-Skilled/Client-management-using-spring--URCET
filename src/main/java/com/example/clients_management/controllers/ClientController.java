package com.example.clients_management.controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.clients_management.dto.BookingRequest;
import com.example.clients_management.entities.Subcategory;
import com.example.clients_management.entities.ServiceProviderCharge;
import com.example.clients_management.entities.Bookings;
import com.example.clients_management.entities.ClientDetails;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.entities.CartItem;
import com.example.clients_management.repositories.BookingsRepository;
import com.example.clients_management.repositories.ClientRepository;
import com.example.clients_management.repositories.ServiceProviderRepository;
import com.example.clients_management.service.EmailService;
import com.example.clients_management.service.ServiceProviderChargeService;
import com.example.clients_management.service.CartService;
import com.example.clients_management.service.SubcategoryService;

import com.example.clients_management.entities.Category;
import com.example.clients_management.repositories.CategoryRepository;
import com.example.clients_management.entities.Service;
import com.example.clients_management.repositories.ServiceRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ClientController {

	@Autowired
	private EmailService emailService;

	@Autowired
	EmailService emailservice;

	@Autowired
	private BookingsRepository bookingsRepository;
	
	@Autowired
	ServiceProviderRepository spr;

	@Autowired
	ClientRepository clientRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private ServiceProviderRepository serviceProviderRepository;

	@GetMapping("/")
	public String land(HttpSession session, Model model) {
		String clientEmail = (String) session.getAttribute("clientEmail");
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		
		if (clientEmail != null) {
			return "clientDashboard";
		}
		return "index";
	}

	@GetMapping("/services-by-category/{categoryId}")
	@ResponseBody
	public List<Service> getServicesByCategory(@PathVariable Long categoryId) {
		return serviceRepository.findByCategory_Id(categoryId);
	}


	@GetMapping("/clientlogin")
	public String login() {
		return "clientLogin";
	}

	@GetMapping("/clientsignup")
	public String client_signup(Model model) {
		model.addAttribute("clientDetails", new ClientDetails());
		return "clientRegister";
	}

	@PostMapping("/clientsignup")
	public String clientsign(@ModelAttribute ClientDetails client, Model model) {
		try {
			// Check if the username, email, or mobile number already exists
			if (clientRepository.existsByName(client.getName())) {
				model.addAttribute("error", "Username is already taken.");
				model.addAttribute("clientDetails", client);
				return "clientRegister";
			}

			if (clientRepository.existsByEmail(client.getEmail())) {
				model.addAttribute("error", "Email is already registered.");
				model.addAttribute("clientDetails", client);
				return "clientRegister";
			}

			if (clientRepository.existsByMobile(client.getMobile())) {
				model.addAttribute("error", "Mobile number is already registered.");
				model.addAttribute("clientDetails", client);
				return "clientRegister";
			}

			if (client.getMobile().length() < 10) {
				model.addAttribute("error", "Mobile number must be at least 10 digits.");
				model.addAttribute("clientDetails", client);
				return "clientRegister";
			}

			// Encode the password using BCryptPasswordEncoder
			client.setPassword(passwordEncoder.encode(client.getPassword()));

			// Save the new client details
			clientRepository.save(client);
			
			// Send welcome email
			String subject = "Welcome to Client Management System";
			String body = "Dear " + client.getName() + ",\n\n" +
						 "Welcome to our Client Management System! Your account has been successfully created.\n\n" +
						 "You can now log in using your email: " + client.getEmail() + "\n\n" +
						 "Best regards,\nClient Management Team";
			emailservice.sendEmail(client.getEmail(), subject, body);
			
			return "redirect:/clientlogin";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("error", "An error occurred during registration. Please try again.");
			model.addAttribute("clientDetails", client);
			return "clientRegister";
		}
	}

	@PostMapping("/clientlogin")
	public String login(String email, String password, Model model, HttpSession session) {
		ClientDetails client = clientRepository.findByEmail(email);

		if (client == null) {
			model.addAttribute("errorMessage", "Email not found");
			return "clientLogin";
		}

		if (!passwordEncoder.matches(password, client.getPassword())) {
			model.addAttribute("errorMessage", "Password is wrong");
			return "clientLogin";
		}

		session.setAttribute("clientEmail", client.getEmail()); // Store the logged-in user in the session
		session.setAttribute("userType", "customer");
		
		// Add categories to model before redirecting to dashboard
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		
		return "clientDashboard"; // Redirect to the homepage or a protected area
	}

	@GetMapping("/clientlogout")
	public String logout(HttpSession session) {
		// Invalidate the session to log out the user
		session.invalidate();
		return "redirect:/clientlogin"; // Redirect to the login page after logout
	}

	@PostMapping("/bookserviceprovider")
	public String bookServiceProvider(@ModelAttribute Bookings bookingRequest, 
                                @RequestParam(value = "cartItemId", required = false) Long cartItemId,
                                HttpSession session) {

		// Retrieve client email from session
		String clientEmail = (String) session.getAttribute("clientEmail");

		if (clientEmail == null) {
			return "redirect:/clientlogin"; // Redirects to login if email not found
		}

		// Fetch client details from the database using clientEmail
		ClientDetails client = clientRepository.findByEmail(clientEmail);
		if (client == null) {
			return "Client not found";
		}

		// Retrieve service provider ID from session
		Long serviceProviderId = (Long) session.getAttribute("serviceProviderId");
		// Create a new booking
		Bookings booking = new Bookings();
		booking.setServiceProviderId(serviceProviderId);
		booking.setBookedBy(client.getName());
		booking.setDate(bookingRequest.getDate()); // Assuming date is a String or properly formatted
		booking.setTime(bookingRequest.getTime()); // Assuming time is a String or properly formatted
		booking.setAddress(client.getLocation());
		booking.setPhone(client.getMobile());
		booking.setStatus("pending");

		// Save the booking to the database
		try {
			bookingsRepository.save(booking);
			
			// Remove the item from cart if cartItemId is present
			if (cartItemId != null) {
				cartService.removeFromCart(cartItemId);
			}
			
			return "redirect:/cart"; // Changed from "clientDashboard" to "redirect:/cart"
		} catch (Exception e) {
			e.printStackTrace();
			return "Error during booking";
		}
	}

	@GetMapping("/bookserviceprovider")
	public String initiateBooking(@RequestParam("id") Long serviceProviderId, 
                            @RequestParam(value = "cartItemId", required = false) Long cartItemId,
                            HttpSession session, 
                            Model model) {

		// Store the serviceProviderId in the session
		session.setAttribute("serviceProviderId", serviceProviderId);

		// Fetch service provider details and add to model
		ServiceProviderDetails provider = spr.findById(serviceProviderId)
				.orElseThrow(() -> new RuntimeException("Service provider not found"));
		model.addAttribute("provider", provider);
		
		// If cartItemId is present, fetch and add cart item to model
		if (cartItemId != null) {
			CartItem cartItem = cartService.getCartItem(cartItemId);
			model.addAttribute("cartItem", cartItem);
		}
		
		model.addAttribute("cartItemId", cartItemId); // Pass cartItemId to the form

		// Redirect to the booking form page
		return "bookingForm"; // Make sure there is a bookingForm.html in templates folder
	}

	// Method to display all bookings for the logged-in user
	@GetMapping("/clientbookings")
	public String customerBookings(Model model, HttpSession session) {
		ClientDetails client = clientRepository.findByEmail((String)session.getAttribute("clientEmail"));
		
		if (client.getEmail() == null) {
			return "redirect:/clientlogin"; // Redirect to login page if no email is found in session
		}

		// Fetch the bookings for the logged-in user
		List<Bookings> bookings = bookingsRepository.findByBookedBy(client.getName()); // Modify as needed (e.g., find by
																					// email)

		 // Enrich bookings with service provider details
        bookings.forEach(booking -> {
            ServiceProviderDetails provider = spr.findById(booking.getServiceProviderId())
                .orElse(new ServiceProviderDetails()); // Fallback to empty object if not found
            booking.setServiceProvider(provider);
        });

		// Add the bookings to the model
		model.addAttribute("bookings", bookings);
		model.addAttribute("userType",session.getAttribute("userType"));

		return "clientBookings"; // Return the clientBookings template
	}
	
	@GetMapping("/cancelbooking/{id}")
	public String cancelBooking(@PathVariable Long id, HttpSession session) {
	    // Check if userType exists in session
	    String userType = (String) session.getAttribute("userType");
	    if (userType == null) {
	        return "redirect:/login"; // Redirect to login page if userType is not found
	    }

	    // Check if the booking exists
	    Optional<Bookings> bookingOptional = bookingsRepository.findById(id);
	    if (bookingOptional.isPresent()) {
	        Bookings booking = bookingOptional.get();
	        bookingsRepository.delete(booking);
	    }

	    // Redirect based on user type
	    if ("customer".equals(userType)) {
	        return "redirect:/client/mybookings";
	    } else {
	        return "redirect:/mybookings";
	    }
	}
	
	@GetMapping("/bookingdetails/{id}")
	public String bookingDetails(@PathVariable long id, Model model) {
	    // Retrieve the booking by its ID
	    Bookings booking = bookingsRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("No booking found for the specified ID: " + id));

	    // Retrieve the service provider details
	    Optional<ServiceProviderDetails> serviceProviderDetails = spr.findById(booking.getServiceProviderId());
	    if (serviceProviderDetails.isPresent()) {
	        model.addAttribute("serviceProvider", serviceProviderDetails.get());
	    } else {
	        throw new RuntimeException("No service provider found for the booking ID: " + id);
	    }

	    // Add the booking to the model
	    model.addAttribute("bookings", booking);

	    return "bookingdetails"; // Renders the bookingdetails.html template
	}
	
	@GetMapping("/cancelacceptedbooking/{id}")
	public String cancelacceptedBooking(@PathVariable Long id, HttpSession session) {
	    // Check if userType exists in session
	    String userType = (String) session.getAttribute("userType");
	    if (userType == null) {
	        return "redirect:/login"; // Redirect to login page if userType is not found
	    }

	    // Check if the booking exists
	    Optional<Bookings> bookingOptional = bookingsRepository.findById(id);
	    if (bookingOptional.isPresent()) {
	        Bookings booking = bookingOptional.get();
	            bookingsRepository.delete(booking);
	    }

	    // Redirect back to the 'mybookings' page after deletion
	    return "redirect:/acceptedbookings";
	}

	@PostMapping("/addToCart")
	@ResponseBody
	public ResponseEntity<String> addToCart(@RequestBody Map<String, Object> request, HttpSession session) {
	    String clientEmail = (String) session.getAttribute("clientEmail");
	    if (clientEmail == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
	    }

	    Long providerId = Long.parseLong(request.get("providerId").toString());
	    Integer quantity = Integer.parseInt(request.get("quantity").toString());
	    
	    ServiceProviderDetails provider = spr.findById(providerId)
	        .orElseThrow(() -> new RuntimeException("Provider not found"));

	    cartService.addToCart(provider, clientEmail, quantity);
	    return ResponseEntity.ok("Added to cart successfully");
	}

	@GetMapping("/cart")
	public String viewCart(Model model, HttpSession session) {
	    String clientEmail = (String) session.getAttribute("clientEmail");
	    if (clientEmail == null) {
	        return "redirect:/clientlogin";
	    }
	    
	    List<CartItem> cartItems = cartService.getCartItems(clientEmail);
	    model.addAttribute("cartItems", cartItems);
	    return "cart";
	}

	@DeleteMapping("/removeFromCart/{id}")
    @ResponseBody
    public ResponseEntity<String> removeFromCart(@PathVariable Long id, HttpSession session) {
        String clientEmail = (String) session.getAttribute("clientEmail");
        if (clientEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        
        cartService.removeFromCart(id);
        return ResponseEntity.ok("Item removed successfully");
    }

	@PostMapping("/submitRating")
	@ResponseBody
	public ResponseEntity<Map<String, Boolean>> submitRating(@RequestBody Map<String, Object> payload) {
	    try {
	        Long bookingId = Long.parseLong(payload.get("bookingId").toString());
	        Integer rating = Integer.parseInt(payload.get("rating").toString());
	        
	        Optional<Bookings> bookingOpt = bookingsRepository.findById(bookingId);
	        if (bookingOpt.isPresent()) {
	            Bookings booking = bookingOpt.get();
	            
	            // Update service provider rating directly
	            ServiceProviderDetails provider = spr.findById(booking.getServiceProviderId())
	                .orElseThrow(() -> new RuntimeException("Service provider not found"));
	            
	            // Calculate new rating
	            int newNumberOfRatings = provider.getNumberOfRatings() + 1;
	            double currentTotalRating = provider.getAvgRating() * provider.getNumberOfRatings();
	            double newAvgRating = (currentTotalRating + rating) / newNumberOfRatings;
	            
	            // Update provider
	            provider.setNumberOfRatings(newNumberOfRatings);
	            provider.setAvgRating(newAvgRating);
	            spr.save(provider);
	            
	            // Update booking status to "RATED" and save
	            booking.setStatus("RATED");
	            bookingsRepository.save(booking);
	            
	            return ResponseEntity.ok(Map.of("success", true));
	        }
	        return ResponseEntity.badRequest().body(Map.of("success", false));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                           .body(Map.of("success", false));
	    }
	}
	@GetMapping("/client/services/{serviceId}")
	public String viewServiceSubcategories(@PathVariable Long serviceId, Model model) {
		List<Subcategory> subcategories = subcategoryService.getSubcategoriesByServiceId(serviceId);
		model.addAttribute("subcategories", subcategories);
		return "subcategories";
	}

	@Autowired
	ServiceProviderChargeService serviceProviderChargeService;



	@GetMapping("/client/subcategory/{subcategoryId}/providers")
	public String viewServiceProviders(@PathVariable Long subcategoryId, Model model, HttpSession session) {
		// Get client email from session
		String clientEmail = (String) session.getAttribute("clientEmail");
		if (clientEmail == null) {
			return "redirect:/client/login";
		}
		session.setAttribute("subId",subcategoryId);	

		// Get service providers for the subcategory
		List<ServiceProviderDetails> providers = serviceProviderChargeService.getServiceProvidersBySubcategoryId(subcategoryId);
		
		// Get charge details for each provider
		Map<Long, ServiceProviderCharge> chargeDetails = new HashMap<>();
		for (ServiceProviderDetails provider : providers) {
			ServiceProviderCharge charge = serviceProviderChargeService.getChargeByProviderAndSubcategory(provider.getId(), subcategoryId);
			chargeDetails.put(provider.getId(), charge);
		}
		
		// Add attributes to model
		model.addAttribute("providers", providers);
		model.addAttribute("chargeDetails", chargeDetails);
		model.addAttribute("clientEmail", clientEmail);
		
		return "service-providers";
	}

	@GetMapping("/client/mybookings")
	public String viewClientBookings(Model model, HttpSession session) {
		// Get client email from session
		String clientEmail = (String) session.getAttribute("clientEmail");
		if (clientEmail == null) {
			return "redirect:/client/login";
		}

		// Get all bookings for the client
		List<Bookings> bookings = bookingsRepository.findByBookedBy(clientEmail);

		// Add attributes to model
		model.addAttribute("bookings", bookings);
		model.addAttribute("userType", "customer");
		
		// Return the mybookings view
		return "mybookings";
	}

	@PostMapping("/client/create-booking")
public String createBooking(
    @RequestParam("providerId") Long providerId,
    @RequestParam("bookingDate") String bookingDate,
    @RequestParam("bookingTime") String bookingTime,
    @RequestParam(value = "notes", required = false) String notes,
    HttpSession session,
    RedirectAttributes redirectAttributes) {  // Added RedirectAttributes to store messages

    try {
        // Get client email from session
        String clientEmail = (String) session.getAttribute("clientEmail");
        if (clientEmail == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to book a service.");
            return "redirect:/client/mybookings"; // Redirect with error message
        }

        // Get service provider details
        Optional<ServiceProviderDetails> providerOpt = spr.findById(providerId);
        if (!providerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Service provider not found.");
            return "redirect:/client/mybookings";
        }
        ServiceProviderDetails provider = providerOpt.get();

        // Get client details
        ClientDetails client = clientRepository.findByEmail(clientEmail);
        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Client details not found.");
            return "redirect:/client/mybookings";
        }

        // Create booking
        Bookings booking = new Bookings();
        booking.setServiceProviderId(providerId);
        booking.setBookedBy(clientEmail);
        booking.setDate(bookingDate);
        booking.setTime(bookingTime);
        booking.setAddress(client.getLocation());
        booking.setPhone(client.getMobile());
        booking.setStatus("PENDING");

        // Save booking
        bookingsRepository.save(booking);

        // Send notification email to provider
        String providerMessage = String.format(
            "New booking request from %s\nDate: %s\nTime: %s\nContact: %s\nAddress: %s",
            clientEmail, bookingDate, bookingTime, client.getMobile(), client.getLocation()
        );
        emailservice.sendEmail(provider.getEmail(), "New Booking Request", providerMessage);

        // Send confirmation email to client
        String clientMessage = String.format(
            "Your booking with %s has been created\nDate: %s\nTime: %s\nStatus: %s\nThe service provider will contact you shortly.",
            provider.getName(), bookingDate, bookingTime, booking.getStatus()
        );
        emailservice.sendEmail(clientEmail, "Booking Confirmation", clientMessage);

        redirectAttributes.addFlashAttribute("success", "Booking created successfully.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
    }

    return "redirect:/client/mybookings"; // Always redirect to "mybookings"
}

	@PostMapping("/complete-booking/{id}")
    public String completeBooking(@PathVariable Long id, 
                                @RequestParam Integer rating,
                                @RequestParam(required = false) String feedback,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            // Get the booking
            Bookings booking = bookingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Get the service provider
            ServiceProviderDetails provider = serviceProviderRepository.findById(booking.getServiceProviderId())
                .orElseThrow(() -> new RuntimeException("Service provider not found"));

            // Update booking status
            booking.setStatus("COMPLETED");
            bookingsRepository.save(booking);

            // Update provider's average rating
            Double currentAvgRating = provider.getAvgRating() != null ? provider.getAvgRating() : 0.0;
            Integer currentNumRatings = provider.getNumberOfRatings() != null ? provider.getNumberOfRatings() : 0;

            // Calculate new average rating
            currentNumRatings++;
            double newAvgRating = ((currentAvgRating * (currentNumRatings - 1)) + rating) / currentNumRatings;

            provider.setAvgRating(newAvgRating);
            provider.setNumberOfRatings(currentNumRatings);
            serviceProviderRepository.save(provider);

            // Send thank you email to client
            String message = String.format(
                "Thank you for using our service!\n" +
                "We appreciate your feedback and rating of %d stars.\n" +
                "Looking forward to serving you again!",
                rating
            );
            emailService.sendEmail(booking.getBookedBy(), "Booking Completed - Thank You!", message);

            redirectAttributes.addFlashAttribute("success", "Booking completed and rating submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to complete booking: " + e.getMessage());
        }

        return "redirect:/client/mybookings";
    }
	
}
