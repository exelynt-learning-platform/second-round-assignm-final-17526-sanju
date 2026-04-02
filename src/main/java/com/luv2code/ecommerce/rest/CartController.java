package com.luv2code.ecommerce.rest;

import com.luv2code.ecommerce.dto.AddProductToCartRequest;
import com.luv2code.ecommerce.dto.CartItemResponse;
import com.luv2code.ecommerce.dto.CartResponse;
import com.luv2code.ecommerce.dto.UpdateQuantityRequest;
import com.luv2code.ecommerce.entity.Cart;
import com.luv2code.ecommerce.entity.CartItem;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.service.CartService;
import com.luv2code.ecommerce.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    /**
     * Get current user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized cart access attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
            }

            Cart cart = cartService.viewCart(user.getId());
            List<CartItemResponse> cartItemResponses = convertCartItemsToResponse(cart.getCartItems());
            CartResponse response = new CartResponse(cart.getId(), cart.getTotalPrice(), cart.getTotalQuantity(), cartItemResponses);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Cart retrieval validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error retrieving cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to retrieve cart"));
        }
    }

    /**
     * Add product to cart
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addProductToCart(@Valid @RequestBody AddProductToCartRequest request) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized add to cart attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
            }

            CartItem cartItem = cartService.addProductToCart(user.getId(), request.getProductId(), request.getQuantity());
            Cart cart = cartService.viewCart(user.getId());

            List<CartItemResponse> cartItemResponses = convertCartItemsToResponse(cart.getCartItems());
            CartResponse cartResponse = new CartResponse(cart.getId(), cart.getTotalPrice(), cart.getTotalQuantity(), cartItemResponses);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            response.put("cart", cartResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Add to cart validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error adding product to cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/update/{cartItemId}
     */
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized cart update attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
            }

            CartItem updatedItem = cartService.updateCartItemQuantity(user.getId(), cartItemId, request.getQuantity());
            Cart cart = cartService.viewCart(user.getId());

            List<CartItemResponse> cartItemResponses = convertCartItemsToResponse(cart.getCartItems());
            CartResponse cartResponse = new CartResponse(cart.getId(), cart.getTotalPrice(), cart.getTotalQuantity(), cartItemResponses);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart item updated successfully");
            response.put("cart", cartResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Cart update validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error updating cart item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Remove product from cart
     * DELETE /api/cart/remove/{cartItemId}
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long cartItemId) {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized cart removal attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
            }

            cartService.removeProductFromCart(user.getId(), cartItemId);
            Cart cart = cartService.viewCart(user.getId());

            List<CartItemResponse> cartItemResponses = convertCartItemsToResponse(cart.getCartItems());
            CartResponse cartResponse = new CartResponse(cart.getId(), cart.getTotalPrice(), cart.getTotalQuantity(), cartItemResponses);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product removed from cart");
            response.put("cart", cartResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Cart removal validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error removing product from cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Clear entire cart
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                logger.warn("Unauthorized cart clear attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("User not authenticated"));
            }

            cartService.clearCart(user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart cleared successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error clearing cart: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to clear cart"));
        }
    }

    private List<CartItemResponse> convertCartItemsToResponse(Set<CartItem> cartItems) {
        List<CartItemResponse> items = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                BigDecimal totalPrice = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                CartItemResponse itemResponse = new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        totalPrice
                );
                items.add(itemResponse);
            }
        }
        return items;
    }
}
