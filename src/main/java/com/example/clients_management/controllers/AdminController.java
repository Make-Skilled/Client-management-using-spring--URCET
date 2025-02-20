package com.example.clients_management.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import com.example.clients_management.entities.ClientDetails;
import com.example.clients_management.entities.ServiceProviderDetails;
import com.example.clients_management.entities.AdminDetails;
import com.example.clients_management.entities.Bookings;
import com.example.clients_management.entities.Category;
import com.example.clients_management.entities.Subcategory;
import com.example.clients_management.repositories.AdminRepository;
import com.example.clients_management.repositories.ClientRepository;
import com.example.clients_management.repositories.ServiceProviderRepository;
import com.example.clients_management.repositories.BookingsRepository;
import com.example.clients_management.repositories.CategoryRepository;
import com.example.clients_management.repositories.SubcategoryRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private BookingsRepository bookingsRepository;  

    @Autowired
    private CategoryRepository categoryRepository;  

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    private static final String UPLOAD_DIR = "uploads";

    private String saveFile(MultipartFile file, String directory) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    @GetMapping("/adminlogin")
    public String showLoginPage() {
        return "adminLogin";
    }

    @PostMapping("/adminlogin")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        AdminDetails admin = adminRepository.findByEmail(email);
        if (admin == null) {
            model.addAttribute("errorMessage", "Email not found");
            return "adminLogin";
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            model.addAttribute("errorMessage", "Password is wrong");
            return "adminLogin";
        }
        session.setAttribute("adminId", admin.getId());
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }

        // Add statistics
        long clientCount = clientRepository.count();
        long providerCount = serviceProviderRepository.count();
        long categoryCount = categoryRepository.count();
        long bookingCount = bookingsRepository.count();

        model.addAttribute("clientCount", clientCount);
        model.addAttribute("serviceProviderCount", providerCount);
        model.addAttribute("categoryCount", categoryCount);
        model.addAttribute("bookingCount", bookingCount);
        return "adminDashboard";
    }

    @GetMapping("/admin/addcategory")
    public String showAddCategory(HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        return "addCategory";
    }

    @PostMapping("/admin/addcategory")
    @ResponseBody
    public Map<String, Object> addCategory(@RequestParam("name") String name,
                                         @RequestParam("description") String description,
                                         @RequestParam(value = "image", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name is required");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Category description is required");
            }

            // Check if category name already exists
            if (categoryRepository.findByName(name) != null) {
                throw new IllegalArgumentException("Category with this name already exists");
            }

            Category category = new Category();
            category.setName(name.trim());
            category.setDescription(description.trim());

            if (file != null && !file.isEmpty()) {
                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("Only image files are allowed");
                }

                // Validate file size (max 5MB)
                if (file.getSize() > 5 * 1024 * 1024) {
                    throw new IllegalArgumentException("File size should not exceed 5MB");
                }

                String fileName = saveFile(file, UPLOAD_DIR);
                category.setImageUrl("/uploads/" + fileName);
            }

            categoryRepository.save(category);
            response.put("success", true);
            response.put("message", "Category added successfully");
            response.put("category", category);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/admin/viewcategories")
    public String viewCategories(Model model, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "viewCategories";
    }

    @GetMapping("/admin/clients")
    public String showClients(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        model.addAttribute("clients", clientRepository.findAll());
        return "clientList";
    }

    @GetMapping("/admin/serviceproviders")
    public String showServiceProviders(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        model.addAttribute("providers", serviceProviderRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll()); 
        return "serviceProviderList";
    }

    @GetMapping("/admin/servicerequests")
    public String showServiceRequests(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        // Fetch all bookings and add to model
        List<Bookings> bookings = bookingsRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "serviceRequests";  
    }

    @GetMapping("/admin/servicehistory")
    public String showServiceHistory(HttpSession session, Model model) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        List<Bookings> bookings = bookingsRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "serviceHistory";
    }

    @GetMapping("/admin/deletecategory/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        
        try {
            // First get the category to find its name
            Category category = categoryRepository.findById(id).orElse(null);
            if (category != null) {
                // Delete all service providers with matching preferred service
                List<ServiceProviderDetails> providers = serviceProviderRepository.findByPreferredService(category.getName());
                serviceProviderRepository.deleteAll(providers);
                
                // Then delete the category
                categoryRepository.deleteById(id);
            }
        } catch (Exception e) {
            // Handle any errors if needed
        }
        
        return "redirect:/admin/viewcategories";
    }

    @DeleteMapping("/admin/deletecategory/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            // Delete the image file if it exists
            if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
                String fileName = category.getImageUrl().substring(category.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.deleteIfExists(filePath);
            }

            categoryRepository.delete(category);
            response.put("success", true);
            response.put("message", "Category deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/admin/deleterequest/{id}")
    public String deleteServiceRequest(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        
        try {
            bookingsRepository.deleteById(id);
        } catch (Exception e) {
            // Handle any errors if needed
        }
        
        return "redirect:/admin/servicerequests";
    }

    @GetMapping("/admin/deletehistory/{id}")
    public String deleteServiceHistory(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        
        try {
            bookingsRepository.deleteById(id);
        } catch (Exception e) {
            // Handle any errors if needed
        }
        
        return "redirect:/admin/servicehistory";
    }

    @GetMapping("/admin/deleteclient/{id}")
    public String deleteClient(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        
        try {
            clientRepository.deleteById(id);
        } catch (Exception e) {
            // Handle any errors if needed
        }
        
        return "redirect:/admin/clients";
    }

    @GetMapping("/admin/deleteprovider/{id}")
    public String deleteServiceProvider(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }
        
        try {
            serviceProviderRepository.deleteById(id);
        } catch (Exception e) {
            // Handle any errors if needed
        }
        
        return "redirect:/admin/serviceproviders";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/adminlogin";
    }

    @PostMapping("/admin/addclient")
    public String addClient(@RequestParam String name,
                          @RequestParam String mobile,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String location,
                          HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }

        ClientDetails client = new ClientDetails();
        client.setName(name);
        client.setMobile(mobile);
        client.setEmail(email);
        client.setPassword(passwordEncoder.encode(password));
        client.setLocation(location);

        clientRepository.save(client);
        return "redirect:/admin/clients";
    }

    @PostMapping("/admin/addprovider")
    public String addServiceProvider(@RequestParam String name,
                                   @RequestParam String mobile,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String location,
                                   @RequestParam String preferredService,
                                   @RequestParam double budget,
                                   HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }

        ServiceProviderDetails provider = new ServiceProviderDetails();
        provider.setName(name);
        provider.setMobile(mobile);
        provider.setEmail(email);
        provider.setPassword(passwordEncoder.encode(password));
        provider.setLocation(location);
        provider.setPreferredService(preferredService);

        serviceProviderRepository.save(provider);
        return "redirect:/admin/serviceproviders";
    }

    @GetMapping("/admin/subcategories/{categoryId}")
    @ResponseBody
    public List<Subcategory> getSubcategories(@PathVariable Long categoryId) {
        return subcategoryRepository.findByCategoryId(categoryId);
    }

    @GetMapping("/admin/viewsubcategories/{categoryId}")
    public String viewSubcategories(@PathVariable Long categoryId, Model model, HttpSession session) {
        if (session.getAttribute("adminId") == null) {
            return "redirect:/adminlogin";
        }

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        List<Subcategory> subcategories = subcategoryRepository.findByCategoryId(categoryId);

        model.addAttribute("category", category);
        model.addAttribute("subcategories", subcategories);

        return "viewSubcategories";
    }

    @PostMapping("/admin/addsubcategory")
    @ResponseBody
    public Map<String, Object> addSubcategory(
            @RequestParam("parentCategoryId") Long categoryId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

            String fileName = saveFile(image, UPLOAD_DIR);

            Subcategory subcategory = new Subcategory();
            subcategory.setName(name);
            subcategory.setDescription(description);
            subcategory.setImageUrl("/uploads/" + fileName);
            subcategory.setCategory(category);

            subcategoryRepository.save(subcategory);

            response.put("success", true);
            response.put("message", "Subcategory added successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/admin/deletesubcategory/{id}")
    @ResponseBody
    public Map<String, Object> deleteSubcategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get subcategory to delete its image
            Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

            // Delete image file if it exists
            if (subcategory.getImageUrl() != null) {
                String imagePath = new File(".").getCanonicalPath() + File.separator + 
                                 UPLOAD_DIR + File.separator + subcategory.getImageUrl();
                Files.deleteIfExists(Paths.get(imagePath));
            }

            subcategoryRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Subcategory deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}