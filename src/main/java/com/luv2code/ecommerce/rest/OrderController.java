package com.luv2code.ecommerce.rest;

import com.luv2code.ecommerce.dto.OrderItemRequest;
import com.luv2code.ecommerce.dto.OrderRequest;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.entity.OrderItem;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.dao.OrderItemRepository;
import com.luv2code.ecommerce.dao.OrderRepository;
import com.luv2code.ecommerce.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AuthService authService;

    /**
     * Create a new order
     * POST /api/orders/create
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                String errors = bindingResult.getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Validation failed: " + errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
            }

            User currentUser = authService.getCurrentUser();

            Order order = new Order();
            order.setOrderTrackingNumber(generateOrderTrackingNumber());
            order.setTotalPrice(orderRequest.getTotalPrice());
            order.setTotalQuantity(orderRequest.getTotalQuantity());
            order.setCustomerEmail(orderRequest.getCustomerEmail());
            order.setBillingAddress(orderRequest.getBillingAddress());
            order.setShippingAddress(orderRequest.getShippingAddress());
            order.setStatus("pending");
            if (currentUser != null) {
                order.setUser(currentUser);
            }

            Order savedOrder = orderRepository.save(order);

            if (orderRequest.getOrderItems() != null && !orderRequest.getOrderItems().isEmpty()) {
                Set<OrderItem> orderItems = new HashSet<>();
                for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setUnitPrice(itemRequest.getUnitPrice());
                    orderItem.setImageUrl(itemRequest.getImageUrl());
                    orderItem.setOrder(savedOrder);
                    orderItems.add(orderItemRepository.save(orderItem));
                }
                savedOrder.setOrderItems(orderItems);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getId());
            response.put("orderTrackingNumber", savedOrder.getOrderTrackingNumber());
            response.put("totalPrice", savedOrder.getTotalPrice());
            response.put("totalQuantity", savedOrder.getTotalQuantity());
            response.put("status", savedOrder.getStatus());
            response.put("message", "Order created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get order by ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        try {
            Optional<Order> order = orderRepository.findById(orderId);
            if (order.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Order not found"));
            }
            return ResponseEntity.ok(order.get());
        } catch (Exception e) {
            logger.error("Error retrieving order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get all orders for a customer email
     * GET /api/orders/customer/{email}
     */
    @GetMapping("/customer/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrdersByEmail(@PathVariable String email) {
        try {
            List<Order> orders = orderRepository.findByCustomerEmail(email);
            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("No orders found for customer"));
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get orders for current authenticated user
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyOrders() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Not authenticated"));
            }
            List<Order> orders = orderRepository.findByCustomerEmail(currentUser.getEmail());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving user orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update order status
     * PUT /api/orders/{orderId}
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Order not found"));
            }

            Order order = orderOpt.get();
            String newStatus = request.get("status");
            if (newStatus != null && !newStatus.isEmpty()) {
                order.setStatus(newStatus);
                orderRepository.save(order);
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error updating order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private String generateOrderTrackingNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
