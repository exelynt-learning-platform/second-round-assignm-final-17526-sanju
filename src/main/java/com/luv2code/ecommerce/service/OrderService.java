package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.CartItemRepository;
import com.luv2code.ecommerce.dao.CartRepository;
import com.luv2code.ecommerce.dao.OrderItemRepository;
import com.luv2code.ecommerce.dao.OrderRepository;
import com.luv2code.ecommerce.dao.ProductRepository;
import com.luv2code.ecommerce.entity.Cart;
import com.luv2code.ecommerce.entity.CartItem;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.entity.OrderItem;
import com.luv2code.ecommerce.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Place an order from the user's current cart
     */
    public Order placeOrderFromCart(User user, String billingAddress, String shippingAddress) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderTrackingNumber(generateTrackingNumber());
        order.setTotalPrice(cart.getTotalPrice());
        order.setTotalQuantity(cart.getTotalQuantity());
        order.setCustomerEmail(user.getEmail());
        order.setBillingAddress(billingAddress);
        order.setShippingAddress(shippingAddress);
        order.setStatus("pending");
        order.setOrderItems(new HashSet<>());

        Order savedOrder = orderRepository.save(order);

        // Convert cart items to order items
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setImageUrl(cartItem.getProduct().getImageUrl());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItemRepository.save(orderItem));
        }
        savedOrder.setOrderItems(orderItems);

        // Clear cart after order placed
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);

        return savedOrder;
    }

    /**
     * Get order by ID
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Get all orders for a customer email
     */
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Cancel an order
     */
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }

    private String generateTrackingNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
