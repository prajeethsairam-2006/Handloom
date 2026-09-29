package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.*;
import com.handloom.marketplace.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/artisan")
public class ArtisanController {

    private final ArtisanService artisanService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    @Value("${app.upload.dir:e:/handloom-marketplace/uploads/}")
    private String uploadDir;

    public ArtisanController(ArtisanService artisanService,
                             ProductService productService,
                             CategoryService categoryService,
                             OrderService orderService,
                             UserService userService) {
        this.artisanService = artisanService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
    }

    private Artisan getAuthenticatedArtisan(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required.");
        }
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return artisanService.findByUserId(user.getId());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Artisan artisan = getAuthenticatedArtisan(principal);

        int totalProducts = productService.countByArtisan(artisan.getId());
        int inStockProducts = productService.countInStockByArtisan(artisan.getId());
        int lowStockProducts = productService.countLowStockByArtisan(artisan.getId(), 5);
        int ordersReceived = orderService.countOrdersByArtisan(artisan.getId());
        int totalProductsSold = orderService.countProductsSoldByArtisan(artisan.getId());

        List<Order> recentOrders = orderService.findByArtisanId(artisan.getId());
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }

        model.addAttribute("artisan", artisan);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("inStockProducts", inStockProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("ordersReceived", ordersReceived);
        model.addAttribute("totalProductsSold", totalProductsSold);
        model.addAttribute("recentOrders", recentOrders);

        return "artisan/dashboard";
    }

    @GetMapping("/products")
    public String listProducts(Model model, Principal principal) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        List<Product> products = productService.findByArtisanId(artisan.getId());
        model.addAttribute("artisan", artisan);
        model.addAttribute("products", products);
        return "artisan/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model, Principal principal) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        Product product = new Product();
        product.setArtisanId(artisan.getId());
        product.setStatus("ACTIVE");

        List<Category> categories = categoryService.findAll();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        return "artisan/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        product.setArtisanId(artisan.getId());

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = saveUploadedFile(imageFile);
                product.setImageUrl("/uploads/" + filename);
            } else if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                product.setImageUrl("/images/products/kanchipuram_saree.jpg");
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
            return "redirect:/artisan/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving product: " + e.getMessage());
            return "redirect:/artisan/products/add";
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        Product product = productService.findById(id);

        if (!product.getArtisanId().equals(artisan.getId())) {
            return "redirect:/artisan/products";
        }

        List<Category> categories = categoryService.findAll();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);
        return "artisan/product-form";
    }

    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute("product") Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        Product existing = productService.findById(product.getId());

        if (!existing.getArtisanId().equals(artisan.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized to edit this product.");
            return "redirect:/artisan/products";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = saveUploadedFile(imageFile);
                product.setImageUrl("/uploads/" + filename);
            } else {
                product.setImageUrl(existing.getImageUrl());
            }

            product.setArtisanId(artisan.getId());
            productService.update(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
        }

        return "redirect:/artisan/products";
    }

    @PostMapping("/products/{id}/stock")
    public String updateStock(@PathVariable("id") Long id,
                              @RequestParam("stockQuantity") int stockQuantity,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        Product product = productService.findById(id);

        if (!product.getArtisanId().equals(artisan.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action.");
            return "redirect:/artisan/products";
        }

        try {
            productService.updateStock(id, stockQuantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/artisan/products";
    }

    @PostMapping("/products/{id}/status")
    public String toggleStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        Product product = productService.findById(id);

        if (!product.getArtisanId().equals(artisan.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action.");
            return "redirect:/artisan/products";
        }

        product.setStatus(status);
        productService.update(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product status changed to " + status + ".");
        return "redirect:/artisan/products";
    }

    @GetMapping("/orders")
    public String listArtisanOrders(Model model, Principal principal) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        List<Order> orders = orderService.findByArtisanId(artisan.getId());
        model.addAttribute("artisan", artisan);
        model.addAttribute("orders", orders);
        return "artisan/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable("orderId") Long orderId,
                                    @RequestParam("status") String status,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        Artisan artisan = getAuthenticatedArtisan(principal);
        // Verify this order belongs to artisan
        List<Order> artisanOrders = orderService.findByArtisanId(artisan.getId());
        boolean hasOrder = artisanOrders.stream().anyMatch(o -> o.getId().equals(orderId));

        if (!hasOrder) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized order update.");
            return "redirect:/artisan/orders";
        }

        orderService.updateOrderStatus(orderId, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status + ".");
        return "redirect:/artisan/orders";
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String newFilename = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(uploadDir, newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return newFilename;
    }
}
