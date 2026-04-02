package com.luv2code.ecommerce.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {
    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Total quantity must be positive")
    private int totalQuantity;

    @NotBlank(message = "Billing address is required")
    private String billingAddress;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @Valid
    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> orderItems;

    // Constructors
    public OrderRequest() {}

    public OrderRequest(String customerEmail, BigDecimal totalPrice, int totalQuantity, 
                       String billingAddress, String shippingAddress, List<OrderItemRequest> orderItems) {
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.orderItems = orderItems;
    }

    // Getters and Setters
    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }
}
