package com.handloom.marketplace.controller;

import com.handloom.marketplace.exception.InsufficientStockException;
import com.handloom.marketplace.model.Cart;
import com.handloom.marketplace.model.Order;
import com.handloom.marketplace.model.User;
import com.handloom.marketplace.service.CartService;
import com.handloom.marketplace.service.OrderService;
import com.handloom.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    public OrderController(OrderService orderService, CartService cartService, UserService userService) {
        this.orderService = orderService;
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

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        Cart cart = cartService.getCartForCustomer(customer.getId());

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add products before checkout.");
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("customer", customer);
        return "checkout/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@RequestParam("customerName") String customerName,
                             @RequestParam("phone") String phone,
                             @RequestParam("deliveryAddress") String deliveryAddress,
                             @RequestParam("city") String city,
                             @RequestParam("state") String state,
                             @RequestParam("pinCode") String pinCode,
                             @RequestParam("paymentMethod") String paymentMethod,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        User customer = getAuthenticatedCustomer(principal);
        try {
            Order order = orderService.placeOrder(
                    customer.getId(),
                    customerName,
                    phone,
                    deliveryAddress,
                    city,
                    state,
                    pinCode,
                    paymentMethod
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order Number: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId();
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not complete order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders")
    public String listOrders(Model model, Principal principal) {
        User customer = getAuthenticatedCustomer(principal);
        List<Order> orders = orderService.findByCustomerId(customer.getId());
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        User customer = getAuthenticatedCustomer(principal);
        Order order = orderService.findById(id);

        // Security check: Customer can only view their own order
        if (!order.getCustomerId().equals(customer.getId()) && !"ADMIN".equals(customer.getRole())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "orders/details";
    }
}
