package ApplicationLayer.Services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Interfaces.IOrderService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Order;

public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;

    }

    @Override
    public int addOrder(Collection<StoreProductDTO> products, int storeId, int userId, String address, PaymentMethod paymentMethod) {
        List<Integer> productIds = products.stream().map(product -> product.getProductId()).toList();
        IOrder order = new Order(userId, OrderState.PENDING, productIds, storeId, address, paymentMethod);
        orderRepository.addOrder(order);
        return order.getId();
    }

    @Override
    public int updateOrder(int orderId, Collection<Integer> productsIds, int storeID, Integer userId, String address, PaymentMethod paymentMethod) {
        try {
            IOrder updatedOrder = new Order(orderId, userId, OrderState.PENDING, productsIds, storeID,
                    address, paymentMethod);
            orderRepository.updateOrder(orderId, updatedOrder);
            return updatedOrder.getId();

        } catch (IllegalArgumentException e) {
            logger.error("While trying to update, recived error {}", e);
            throw e;
        }
    }

    @Override
    public void deleteOrder(int orderId) {
        try {
            orderRepository.deleteOrder(orderId);
        } catch (IllegalArgumentException e) {
            logger.error("While trying to delete, recived error {}", e);
            throw e;
        }
    }

    @Override
    public IOrder viewOrder(int orderId) {
        try {
            IOrder order = orderRepository.getOrder(orderId);
            return order;
        } catch (IllegalArgumentException e) {
            logger.error("While trying to view, recived error {}", e);
            throw e;
        }
    }

    @Override
    public List<IOrder> searchOrders(String keyword) {
        List<IOrder> orders = new ArrayList<>();
        for (IOrder order : orderRepository.getAllOrders()) {
            if (order.getAddress().contains(keyword) || order.getState().toString().contains(keyword)
                    || order.getPaymentMethod().toString().contains(keyword)
                    || order.getProductIds().toString().contains(keyword)) {
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public int getOrderUserId(int orderId) {
        try {
            IOrder order = orderRepository.getOrder(orderId);
            return order.getUserId();
        } catch (IllegalArgumentException e) {
            logger.error("While trying to get user id, recived error {}", e);
            throw e;
        }
    }

    @Override
    public int getOrderStoreId(int orderId) {
        try {
            IOrder order = orderRepository.getOrder(orderId);
            return order.getStoreId();
        } catch (IllegalArgumentException e) {
            logger.error("While trying to get store id, recived error {}", e);
            throw e;
        }
    }

    @Override
    public List<Integer> getOrderProductIds(int orderId) {
        try {
            IOrder order = orderRepository.getOrder(orderId);
            return new ArrayList<>(order.getProductIds());
        } catch (IllegalArgumentException e) {
            logger.error("While trying to get product ids, recived error {}", e);
            throw e;
        }
    }

    @Override
    public List<IOrder> getOrdersByStoreId(int storeId) {
        Collection<IOrder> orders = orderRepository.getAllOrders();
        List<IOrder> storeOrders = new ArrayList<>();
        for(IOrder order : orders) {
            if(order.getStoreId() == storeId) {
                storeOrders.add(order);
            }
        }
        return storeOrders;


    }
}