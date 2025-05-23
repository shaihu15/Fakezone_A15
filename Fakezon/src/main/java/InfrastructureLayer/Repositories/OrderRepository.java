package InfrastructureLayer.Repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;

@Repository
public class OrderRepository implements IOrderRepository {
    
    private final HashMap<Integer, IOrder> orders; 

    public OrderRepository(HashMap<Integer, IOrder> orders) {
        this.orders = orders;
    }
    
    public OrderRepository() {
        this.orders = new HashMap<>();
    }

    @Override
    public void addOrder(IOrder Order) {
        IOrder existingOrder = orders.get(Order.getId());
        if (existingOrder != null) {
            throw new IllegalArgumentException("Order with ID " + Order.getId() + " already exists.");
        }
        orders.put(Order.getId(), Order);
    }


    @Override
    public void deleteOrder(int orderId) {
        IOrder existingOrder = orders.get(orderId);
        if (existingOrder == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
        }
        orders.remove(orderId);    
    }

    @Override
    public IOrder getOrder(int orderId) {
        IOrder existingOrder = orders.get(orderId);
        if (existingOrder == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " does not exist.");
        }
        return existingOrder;    
    }

    @Override
    public List<IOrder> getAllOrders() {
        return new ArrayList<>(orders.values());    
    }

    @Override
    public void clearAllData() {
        orders.clear();
    }
}