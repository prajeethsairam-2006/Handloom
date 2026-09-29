package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.InsufficientStockException;
import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.*;
import com.handloom.marketplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        ProductRepository productRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Order placeOrder(Long customerId, String customerName, String phone,
                           String deliveryAddress, String city, String state, String pinCode,
                           String paymentMethod) {

        // 1. Validate Cart
        Cart cart = cartRepository.findOrCreateCart(customerId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Your shopping cart is empty. Please add products before checking out.");
        }

        // 2. Validate Stock for all items in Java Service code
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductName()));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + product.getName() + "'. Available: " +
                        product.getStockQuantity() + ", Ordered: " + item.getQuantity()
                );
            }

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);
        }

        // 3. Generate Order Number
        int year = Year.now().getValue();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String orderNumber = String.format("ORD-%d-%s", year, uniqueSuffix);

        // 4. Create Order
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(customerId);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("PLACED");
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setDeliveryAddress(deliveryAddress);
        order.setCity(city);
        order.setState(state);
        order.setPinCode(pinCode);

        Long orderId = orderRepository.save(order);
        order.setId(orderId);

        // 5. Create Order Items & 6. Reduce Product Stock
        for (CartItem item : items) {
            Product product = productRepository.findById(item.getProductId()).get();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(product.getId());
            orderItem.setArtisanId(product.getArtisanId());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            orderRepository.saveOrderItem(orderItem);

            // Stock calculation: New Stock = Current Stock - Ordered Quantity
            int updatedRows = productRepository.reduceStock(product.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new InsufficientStockException("Stock changed during checkout for product: " + product.getName());
            }
        }

        // 7. Save Payment Information
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(paymentMethod);

        if ("ONLINE_DEMO".equalsIgnoreCase(paymentMethod)) {
            payment.setPaymentStatus("PAID");
            order.setOrderStatus("CONFIRMED");
            orderRepository.updateStatus(orderId, "CONFIRMED");
        } else {
            payment.setPaymentStatus("PENDING");
        }

        paymentRepository.save(payment);
        order.setPayment(payment);

        // 8. Clear Customer Cart
        cartRepository.clearCart(cart.getId());

        return order;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> findByArtisanId(Long artisanId) {
        return orderRepository.findByArtisanId(artisanId);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findRecentOrders(int limit) {
        return orderRepository.findRecentOrders(limit);
    }

    public void updateOrderStatus(Long orderId, String newStatus) {
        orderRepository.updateStatus(orderId, newStatus);
    }

    public int countAll() {
        return orderRepository.countAll();
    }

    public int countPendingOrders() {
        return orderRepository.countPendingOrders();
    }

    public BigDecimal getTotalSales() {
        return orderRepository.getTotalSales();
    }

    public int countOrdersByArtisan(Long artisanId) {
        return orderRepository.countOrdersByArtisan(artisanId);
    }

    public int countProductsSoldByArtisan(Long artisanId) {
        return orderRepository.countProductsSoldByArtisan(artisanId);
    }
}
