package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.*;
import com.handloom.marketplace.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ArtisanService artisanService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public AdminController(UserService userService,
                           ArtisanService artisanService,
                           ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           PaymentService paymentService) {
        this.userService = userService;
        this.artisanService = artisanService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        int totalCustomers = userService.countCustomers();
        int totalArtisans = artisanService.countAll();
        int totalProducts = productService.countAll();
        int totalOrders = orderService.countAll();
        int pendingOrders = orderService.countPendingOrders();
        BigDecimal totalSales = orderService.getTotalSales();

        List<Order> recentOrders = orderService.findRecentOrders(5);
        List<Product> lowStockProducts = productService.findLowStock(5);

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalArtisans", totalArtisans);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> customers = userService.findAllCustomers();
        model.addAttribute("customers", customers);
        return "admin/users";
    }

    @GetMapping("/artisans")
    public String listArtisans(Model model) {
        List<Artisan> artisans = artisanService.findAll();
        model.addAttribute("artisans", artisans);
        return "admin/artisans";
    }

    @PostMapping("/artisans/{id}/status")
    public String updateArtisanStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") String status,
                                      RedirectAttributes redirectAttributes) {
        Artisan artisan = artisanService.findById(id);
        artisan.setStatus(status);
        artisanService.update(artisan);
        redirectAttributes.addFlashAttribute("successMessage", "Artisan status updated to " + status + ".");
        return "redirect:/admin/artisans";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               RedirectAttributes redirectAttributes) {
        try {
            if (category.getId() != null && category.getId() > 0) {
                categoryService.update(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
            } else {
                categoryService.save(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category added successfully.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete category: products may be linked to it.");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @PostMapping("/products/{id}/status")
    public String updateProductStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") String status,
                                      RedirectAttributes redirectAttributes) {
        Product product = productService.findById(id);
        product.setStatus(status);
        productService.update(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product status updated to " + status + ".");
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status + ".");
        return "redirect:/admin/orders";
    }

    @GetMapping("/payments")
    public String listPayments(Model model) {
        List<Payment> payments = paymentService.findAll();
        model.addAttribute("payments", payments);
        return "admin/payments";
    }

    @PostMapping("/payments/{id}/status")
    public String updatePaymentStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") String status,
                                      RedirectAttributes redirectAttributes) {
        paymentService.updatePaymentStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Payment status updated to " + status + ".");
        return "redirect:/admin/payments";
    }
}
