package com.luv2code.ecommerce.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.dao.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    public PaymentIntent createPaymentIntent(Long amount, String currency, String email) throws StripeException {
        return PaymentIntent.create(
            PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setReceiptEmail(email)
                .setDescription("Purchase")
                .build()
        );
    }

    public String createPaymentIntentForOrder(Order order) throws StripeException {
        Long amt = order.getTotalPrice().multiply(new java.math.BigDecimal("100")).longValue();
        PaymentIntent paymentIntent = createPaymentIntent(amt, "usd", order.getCustomerEmail());
        
        order.setStripePaymentIntentId(paymentIntent.getId());
        order.setStatus("pending");
        orderRepository.save(order);

        return paymentIntent.getClientSecret();
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public boolean updateOrderStatus(String paymentIntentId) throws StripeException {
        PaymentIntent pi = retrievePaymentIntent(paymentIntentId);
        Order order = orderRepository.findByOrderTrackingNumber(paymentIntentId);

        if (order == null) return false;

        if (pi.getStatus().equals("succeeded")) {
            order.setStatus("completed");
        } else if (pi.getStatus().equals("processing")) {
            order.setStatus("processing");
        } else if (pi.getStatus().equals("requires_payment_method")) {
            order.setStatus("pending");
        }

        orderRepository.save(order);
        return true;
    }

    /**
     * Cancel a payment intent
     *
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return PaymentIntent object from Stripe
     * @throws StripeException if Stripe API call fails
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.cancel();
    }
}
