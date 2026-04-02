package com.luv2code.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    
    private Long cartId;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private List<CartItemResponse> cartItems;

    public CartResponse() {
    }

    public CartResponse(Long cartId, BigDecimal totalPrice, Integer totalQuantity, List<CartItemResponse> cartItems) {
        this.cartId = cartId;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.cartItems = cartItems;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public List<CartItemResponse> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemResponse> cartItems) {
        this.cartItems = cartItems;
    }
}
