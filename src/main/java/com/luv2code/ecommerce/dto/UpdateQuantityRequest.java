package com.luv2code.ecommerce.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateQuantityRequest {
    
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    public UpdateQuantityRequest() {
    }

    public UpdateQuantityRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
