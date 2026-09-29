package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.User;
import com.handloom.marketplace.service.ReviewService;
import com.handloom.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping("/products/{productId}/review")
    public String addReview(@PathVariable("productId") Long productId,
                            @RequestParam("rating") int rating,
                            @RequestParam(value = "comment", required = false) String comment,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        User customer = userService.findByEmail(principal.getName()).orElse(null);
        if (customer == null || !"CUSTOMER".equalsIgnoreCase(customer.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only customers can submit reviews.");
            return "redirect:/products/" + productId;
        }

        try {
            reviewService.addReview(productId, customer.getId(), rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your review has been added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not submit review: " + e.getMessage());
        }

        return "redirect:/products/" + productId;
    }
}
