package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.entity.Cart;
import com.luv2code.ecommerce.entity.CartItem;
import com.luv2code.ecommerce.entity.Product;
import com.luv2code.ecommerce.entity.User;
import com.luv2code.ecommerce.dao.CartRepository;
import com.luv2code.ecommerce.dao.CartItemRepository;
import com.luv2code.ecommerce.dao.ProductRepository;
import com.luv2code.ecommerce.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class CartService {

    private static final int MIN_QUANTITY = 1;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
    }

    public CartItem addProductToCart(Long userId, Long productId, int quantity) {
        Cart cart = getCartByUserId(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (quantity < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }

        if (product.getUnitsInStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        CartItem existing = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        CartItem item;
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (product.getUnitsInStock() < newQty) {
                throw new RuntimeException("Quantity exceeds available stock");
            }
            existing.setQuantity(newQty);
            item = cartItemRepository.save(existing);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getUnitPrice());
            item = cartItemRepository.save(item);
            cart.getCartItems().add(item);
        }

        updateCartTotals(cart);
        return item;
    }

    public void removeProductFromCart(Long userId, Long cartItemId) {
        Cart cart = getCartByUserId(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this user's cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.deleteById(cartItemId);
        cartItemRepository.flush();
        updateCartTotals(cart);
    }

    public CartItem updateCartItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = getCartByUserId(userId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to this user's cart");
        }

        if (quantity < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY);
        }

        Product product = item.getProduct();
        if (product.getUnitsInStock() < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }

        item.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(item);
        updateCartTotals(cart);
        return saved;
    }

    public Cart viewCart(Long userId) {
        return getCartByUserId(userId);
    }

    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cartItemRepository.flush();
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setCartItems(new HashSet<>());
                    cart.setTotalPrice(BigDecimal.ZERO);
                    cart.setTotalQuantity(0);
                    return cartRepository.save(cart);
                });
    }

    private void updateCartTotals(Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalQuantity = 0;

        Set<CartItem> items = cart.getCartItems();
        if (items != null) {
            for (CartItem item : items) {
                BigDecimal itemTotal = item.getUnitPrice()
                        .multiply(new BigDecimal(item.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
                totalQuantity += item.getQuantity();
            }
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
        cartRepository.save(cart);
    }
}
