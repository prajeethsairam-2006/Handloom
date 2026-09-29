package com.handloom.marketplace.controller;

import com.handloom.marketplace.model.Order;
import com.handloom.marketplace.model.User;
import com.handloom.marketplace.service.OrderService;
import com.handloom.marketplace.service.PaymentService;
import com.handloom.marketplace.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, OrderService orderService, UserService userService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/orders/{orderId}/pay")
    public String simulatePayment(@PathVariable("orderId") Long orderId,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        User customer = userService.findByEmail(principal.getName()).orElse(null);
        Order order = orderService.findById(orderId);

        if (customer == null || (!order.getCustomerId().equals(customer.getId()) && !"ADMIN".equals(customer.getRole()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized to update payment for this order.");
            return "redirect:/orders";
        }

        paymentService.markPaidByOrderId(orderId);
        orderService.updateOrderStatus(orderId, "CONFIRMED");
        redirectAttributes.addFlashAttribute("successMessage", "Payment of ₹" + order.getTotalAmount() + " processed successfully! Order status confirmed.");

        return "redirect:/orders/" + orderId;
    }
}
