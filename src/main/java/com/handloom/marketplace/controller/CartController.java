package com.handloom.marketplace.controller;

import com.handloom.marketplace.exception.InsufficientStockException;
import com.handloom.marketplace.model.Cart;
import com.handloom.marketplace.model.User;
import com.handloom.marketplace.service.CartService;
import com.handloom.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    private User getAuthenticatedCustomer(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authentication required.");
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Customer account not found."));
    }

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        User customer = getAuthenticatedCustomer(principal);
        Cart cart = cartService.getCartForCustomer(customer.getId());
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        try {
            cartService.addToCart(customer.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to your cart!");
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + productId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/products/" + productId;
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        try {
            cartService.updateQuantity(customer.getId(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update cart quantity: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("cartItemId") Long cartItemId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        cartService.removeItem(customer.getId(), cartItemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Principal principal, RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        cartService.clearCart(customer.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Cart has been cleared.");
        return "redirect:/cart";
    }
}
