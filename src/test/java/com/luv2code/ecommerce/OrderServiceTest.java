package com.luv2code.ecommerce;

import com.luv2code.ecommerce.entity.*;
import com.luv2code.ecommerce.service.AuthService;
import com.luv2code.ecommerce.dao.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private User createTestUser() {
        return authService.registerUser(
                "ordertest_" + System.nanoTime(),
                "ordertest_" + System.nanoTime() + "@example.com",
                "password123", "Order", "Test");
    }

    @Test
    public void testCreateOrder() {
        User testUser = createTestUser();

        Order order = new Order();
        order.setUser(testUser);
        order.setOrderTrackingNumber("TEST-001");
        order.setTotalPrice(new BigDecimal("99.99"));
        order.setTotalQuantity(1);
        order.setStatus("pending");
        order.setCustomerEmail(testUser.getEmail());
        order.setBillingAddress("123 Main St");
        order.setShippingAddress("456 Oak Ave");
        order.setOrderItems(new HashSet<>());

        Order savedOrder = orderRepository.save(order);

        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertEquals("TEST-001", savedOrder.getOrderTrackingNumber());
        assertEquals(testUser.getId(), savedOrder.getUser().getId());
        assertEquals("pending", savedOrder.getStatus());
    }

    @Test
    public void testOrderToUserRelationship() {
        User testUser = createTestUser();

        Order order = new Order();
        order.setUser(testUser);
        order.setOrderTrackingNumber("TEST-002");
        order.setTotalPrice(new BigDecimal("199.99"));
        order.setTotalQuantity(2);
        order.setStatus("pending");
        order.setCustomerEmail(testUser.getEmail());
        order.setOrderItems(new HashSet<>());

        Order savedOrder = orderRepository.save(order);
        testUser.getOrders().add(savedOrder);
        userRepository.save(testUser);

        User retrievedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(retrievedUser);
        assertTrue(retrievedUser.getOrders().stream()
                .anyMatch(o -> o.getId().equals(savedOrder.getId())));
    }

    @Test
    public void testFindOrderByCustomerEmail() {
        User testUser = createTestUser();

        Order order = new Order();
        order.setUser(testUser);
        order.setOrderTrackingNumber("TEST-003");
        order.setTotalPrice(new BigDecimal("99.99"));
        order.setTotalQuantity(1);
        order.setStatus("pending");
        order.setCustomerEmail(testUser.getEmail());
        order.setOrderItems(new HashSet<>());

        orderRepository.save(order);

        List<Order> orders = orderRepository.findByCustomerEmail(testUser.getEmail());
        assertFalse(orders.isEmpty());
        assertTrue(orders.stream().anyMatch(o -> o.getUser().getId().equals(testUser.getId())));
    }

    @Test
    public void testOrderStatusUpdate() {
        User testUser = createTestUser();

        Order order = new Order();
        order.setUser(testUser);
        order.setOrderTrackingNumber("TEST-004");
        order.setTotalPrice(new BigDecimal("99.99"));
        order.setTotalQuantity(1);
        order.setStatus("pending");
        order.setCustomerEmail(testUser.getEmail());
        order.setOrderItems(new HashSet<>());

        Order savedOrder = orderRepository.save(order);
        assertEquals("pending", savedOrder.getStatus());

        savedOrder.setStatus("completed");
        Order updatedOrder = orderRepository.save(savedOrder);

        assertEquals("completed", updatedOrder.getStatus());
    }

    @Test
    public void testOrderWithItems() {
        User testUser = createTestUser();

        Order order = new Order();
        order.setUser(testUser);
        order.setOrderTrackingNumber("TEST-005");
        order.setTotalPrice(new BigDecimal("149.98"));
        order.setTotalQuantity(2);
        order.setStatus("pending");
        order.setCustomerEmail(testUser.getEmail());
        order.setOrderItems(new HashSet<>());

        Order savedOrder = orderRepository.save(order);

        OrderItem item1 = new OrderItem();
        item1.setProductId(1L);
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("99.99"));
        item1.setImageUrl("http://example.com/img1.jpg");
        item1.setOrder(savedOrder);

        OrderItem item2 = new OrderItem();
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("49.99"));
        item2.setImageUrl("http://example.com/img2.jpg");
        item2.setOrder(savedOrder);

        orderItemRepository.save(item1);
        orderItemRepository.save(item2);

        Order retrieved = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getOrderItems().size());
    }
}
