package InfrastructureLayer.Repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Order;

@Repository
@Primary
@Transactional
public class OrderRepositoryImpl implements IOrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Autowired
    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public void addOrder(IOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        // Check if order already exists
        if (orderJpaRepository.existsById(order.getId())) {
            throw new IllegalArgumentException("Order with ID " + order.getId() + " already exists.");
        }
        
        if (order instanceof Order) {
            orderJpaRepository.save((Order) order);
        } else {
            throw new IllegalArgumentException("Order must be an instance of Order class");
        }
    }

    @Override
    public void deleteOrder(int orderId) {
        if (!orderJpaRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
        }
        orderJpaRepository.deleteById(orderId);
    }

    @Override
    public IOrder getOrder(int orderId) {
        Optional<Order> order = orderJpaRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
        }
        return order.get();
    }

    @Override
    public Collection<IOrder> getAllOrders() {
        return orderJpaRepository.findAll().stream()
                .map(order -> (IOrder) order)
                .toList();
    }

    @Override
    public Collection<IOrder> getOrdersByUserId(int userId) {
        return orderJpaRepository.findByUserId(userId).stream()
                .map(order -> (IOrder) order)
                .toList();
    }

    @Override
    public void clearAllData() {
        orderJpaRepository.deleteAll();
    }

    // Additional methods for business logic
    public Collection<IOrder> getOrdersByStoreId(int storeId) {
        return orderJpaRepository.findByStoreId(storeId).stream()
                .map(order -> (IOrder) order)
                .toList();
    }

    public void updateOrder(IOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (!orderJpaRepository.existsById(order.getId())) {
            throw new IllegalArgumentException("Order with ID " + order.getId() + " does not exist.");
        }
        
        if (order instanceof Order) {
            orderJpaRepository.save((Order) order);
        } else {
            throw new IllegalArgumentException("Order must be an instance of Order class");
        }
    }
} 