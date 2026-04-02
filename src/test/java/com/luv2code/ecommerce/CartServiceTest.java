package com.luv2code.ecommerce;

import com.luv2code.ecommerce.entity.*;
import com.luv2code.ecommerce.service.CartService;
import com.luv2code.ecommerce.service.AuthService;
import com.luv2code.ecommerce.dao.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    public void setUp() {
        // @Transactional on the test rolls back after each test, so no explicit cleanup needed
        // But we still register fresh data for each test

        // Create test user
        testUser = authService.registerUser(
                "carttest_" + System.nanoTime(),
                "carttest_" + System.nanoTime() + "@example.com",
                "password123", "Cart", "Test");

        // Create test category
        ProductCategory category = new ProductCategory();
        category.setCategoryName("Test Category");
        ProductCategory savedCategory = categoryRepository.save(category);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Product Description");
        testProduct.setUnitPrice(new BigDecimal("99.99"));
        testProduct.setImageUrl("https://example.com/image.jpg");
        testProduct.setActive(true);
        testProduct.setUnitsInStock(100);
        testProduct.setSku("TEST-SKU-001");
        testProduct.setCategory(savedCategory);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    public void testAddProductToCart() {
        CartItem cartItem = cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);

        assertNotNull(cartItem);
        assertEquals(testProduct.getId(), cartItem.getProduct().getId());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(testProduct.getUnitPrice(), cartItem.getUnitPrice());
    }

    @Test
    public void testViewCart() {
        cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);

        Cart cart = cartService.viewCart(testUser.getId());

        assertNotNull(cart);
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(new BigDecimal("99.99").multiply(new BigDecimal(2)), cart.getTotalPrice());
    }

    @Test
    public void testUpdateCartItemQuantity() {
        CartItem cartItem = cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);

        CartItem updatedItem = cartService.updateCartItemQuantity(testUser.getId(), cartItem.getId(), 5);

        assertEquals(5, updatedItem.getQuantity());
        Cart cart = cartService.viewCart(testUser.getId());
        assertEquals(5, cart.getTotalQuantity());
    }

    @Test
    public void testRemoveProductFromCart() {
        CartItem cartItem = cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);
        Long cartItemId = cartItem.getId();

        Cart cartBefore = cartService.viewCart(testUser.getId());
        assertEquals(1, cartBefore.getCartItems().size());

        cartService.removeProductFromCart(testUser.getId(), cartItemId);

        assertTrue(cartItemRepository.findById(cartItemId).isEmpty(),
                "CartItem should be deleted after removal");
    }

    @Test
    public void testClearCart() {
        cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);

        Cart cartBefore = cartService.viewCart(testUser.getId());
        assertEquals(1, cartBefore.getCartItems().size());

        cartService.clearCart(testUser.getId());
        Cart cartAfter = cartService.viewCart(testUser.getId());

        assertEquals(0, cartAfter.getCartItems().size());
        assertEquals(0, cartAfter.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, cartAfter.getTotalPrice());
    }

    @Test
    public void testAddProductInsufficientStock() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                cartService.addProductToCart(testUser.getId(), testProduct.getId(), 150)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("stock"));
    }

    @Test
    public void testAddDuplicateProductIncrementsQuantity() {
        cartService.addProductToCart(testUser.getId(), testProduct.getId(), 2);
        cartService.addProductToCart(testUser.getId(), testProduct.getId(), 3);

        Cart cart = cartService.viewCart(testUser.getId());
        assertEquals(1, cart.getCartItems().size());
        assertEquals(5, cart.getTotalQuantity());
    }
}
