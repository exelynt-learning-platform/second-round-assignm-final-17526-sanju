package com.luv2code.ecommerce.dao;

import com.luv2code.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "orderItems", path = "order-items")
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
