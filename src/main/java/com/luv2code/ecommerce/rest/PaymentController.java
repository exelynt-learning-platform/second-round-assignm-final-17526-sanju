package com.luv2code.ecommerce.rest;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.dao.OrderRepository;
import com.luv2code.ecommerce.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Create a payment intent for an order
     * POST /api/payments/create-payment-intent
     * Body: { "orderId": 1 }
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Long> request) {
        try {
            Long orderId = request.get("orderId");

            if (orderId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("orderId is required"));
            }

            Order order = orderRepository.findById(orderId).orElse(null);

            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Order not found"));
            }

            if (order.getOrderTrackingNumber() == null || order.getOrderTrackingNumber().isEmpty()) {
                order.setOrderTrackingNumber(generateOrderTrackingNumber());
                orderRepository.save(order);
            }

            String clientSecret = paymentService.createPaymentIntentForOrder(order);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", clientSecret);
            response.put("paymentIntentId", order.getStripePaymentIntentId());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Stripe error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid payment intent request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request: " + e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Unexpected error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get payment intent status
     * GET /api/payments/payment-intent/{paymentIntentId}
     */
    @GetMapping("/payment-intent/{paymentIntentId}")
    public ResponseEntity<?> getPaymentIntent(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = paymentService.retrievePaymentIntent(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", paymentIntent.getId());
            response.put("status", paymentIntent.getStatus());
            response.put("amount", paymentIntent.getAmount());
            response.put("currency", paymentIntent.getCurrency());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Stripe error retrieving payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Stripe error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid payment intent ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid payment intent ID"));
        } catch (RuntimeException e) {
            logger.error("Error retrieving payment intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving payment intent"));
        }
    }

    /**
     * Update order status based on Stripe payment intent
     * PUT /api/payments/update-order-status
     * Body: { "paymentIntentId": "pi_xxx" }
     */
    @PutMapping("/update-order-status")
    public ResponseEntity<?> updateOrderStatus(@RequestBody Map<String, String> request) {
        try {
            String paymentIntentId = request.get("paymentIntentId");

            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("paymentIntentId is required"));
            }

            boolean success = paymentService.updateOrderStatus(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Order updated successfully" : "Order not found");

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Stripe error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Stripe error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid order status update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request: " + e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error updating order status"));
        }
    }

    /**
     * Cancel a payment intent
     * DELETE /api/payments/cancel/{paymentIntentId}
     */
    @DeleteMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<?> cancelPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent canceledIntent = paymentService.cancelPaymentIntent(paymentIntentId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", canceledIntent.getId());
            response.put("status", canceledIntent.getStatus());
            response.put("message", "Payment canceled successfully");

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            logger.error("Stripe error canceling payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Stripe error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid cancel payment request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid payment intent ID"));
        } catch (RuntimeException e) {
            logger.error("Error canceling payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error canceling payment"));
        }
    }

    /**
     * Get Stripe publishable key config
     * GET /api/payments/config
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Stripe config endpoint. Use publishable key from frontend environment.");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/payments/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    private String generateOrderTrackingNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
