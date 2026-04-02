package com.luv2code.ecommerce.config;

import com.stripe.Stripe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class StripeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfiguration.class);

    @Value("${stripe.api.secret-key:}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()
                || stripeSecretKey.startsWith("sk_test_placeholder")) {
            logger.warn("Stripe secret key is not configured. Payment features will not work. " +
                    "Set STRIPE_SECRET_KEY environment variable for production.");
        } else {
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe API configured successfully.");
        }
    }
}
