package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.Category;
import com.handloom.marketplace.model.Product;
import com.handloom.marketplace.model.Review;
import com.handloom.marketplace.model.User;
import com.handloom.marketplace.service.CategoryService;
import com.handloom.marketplace.service.ProductService;
import com.handloom.marketplace.service.ReviewService;
import com.handloom.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final UserService userService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             ReviewService reviewService,
                             UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                               @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                               Model model) {

        List<Product> products;
        if ((keyword != null && !keyword.trim().isEmpty()) ||
            (categoryId != null && categoryId > 0) ||
            minPrice != null || maxPrice != null) {
            products = productService.search(keyword, categoryId, minPrice, maxPrice);
        } else {
            products = productService.findAllActive();
        }

        List<Category> categories = categoryService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        Product product = productService.findById(id);
        List<Review> reviews = reviewService.findByProductId(id);
        Double averageRating = reviewService.getAverageRating(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);

        boolean canReview = false;
        if (principal != null) {
            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null && "CUSTOMER".equalsIgnoreCase(user.getRole())) {
                boolean alreadyReviewed = reviewService.hasUserReviewed(id, user.getId());
                canReview = !alreadyReviewed;
            }
        }
        model.addAttribute("canReview", canReview);

        return "products/details";
    }

    @GetMapping("/categories/{id}")
    public String productsByCategory(@PathVariable("id") Long id, Model model) {
        return "redirect:/products?categoryId=" + id;
    }
}
