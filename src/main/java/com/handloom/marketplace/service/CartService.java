package com.handloom.marketplace.service;

import com.handloom.marketplace.exception.InsufficientStockException;
import com.handloom.marketplace.exception.ResourceNotFoundException;
import com.handloom.marketplace.model.Cart;
import com.handloom.marketplace.model.CartItem;
import com.handloom.marketplace.model.Product;
import com.handloom.marketplace.repository.CartRepository;
import com.handloom.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getCartForCustomer(Long customerId) {
        Cart cart = cartRepository.findOrCreateCart(customerId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getId());
        cart.setItems(items);
        return cart;
    }

    @Transactional
    public void addToCart(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalArgumentException("This product is currently unavailable.");
        }

        Cart cart = cartRepository.findOrCreateCart(customerId);
        Optional<CartItem> existingItemOpt = cartRepository.findCartItem(cart.getId(), productId);

        int totalRequestedQuantity = quantity;
        if (existingItemOpt.isPresent()) {
            totalRequestedQuantity += existingItemOpt.get().getQuantity();
        }

        if (totalRequestedQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Cannot add " + quantity + " items. Available stock for '" + product.getName() + "' is " + product.getStockQuantity() + "."
            );
        }

        cartRepository.addItem(cart.getId(), productId, quantity);
    }

    @Transactional
    public void updateQuantity(Long customerId, Long cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            cartRepository.removeItem(cartItemId);
            return;
        }

        Cart cart = cartRepository.findOrCreateCart(customerId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getId());
        CartItem targetItem = items.stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Product product = productRepository.findById(targetItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity " + newQuantity + " exceeds available stock (" + product.getStockQuantity() + ")."
            );
        }

        cartRepository.updateItemQuantity(cartItemId, newQuantity);
    }

    @Transactional
    public void removeItem(Long customerId, Long cartItemId) {
        cartRepository.removeItem(cartItemId);
    }

    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = cartRepository.findOrCreateCart(customerId);
        cartRepository.clearCart(cart.getId());
    }

    public int getCartItemCount(Long customerId) {
        Cart cart = cartRepository.findOrCreateCart(customerId);
        return cartRepository.countItems(cart.getId());
    }
}
