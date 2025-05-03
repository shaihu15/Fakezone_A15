package ApplicationLayer.Services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Model.Cart;
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
    public int addOrder(Basket basket, int userId, String address, PaymentMethod paymentMethod) {
        List<StoreProductDTO> products = basket.getProducts();
        List<Integer> productIds = products.stream().map(product -> product.getProductId()).toList();
        IOrder order = new Order(userId, OrderState.PENDING, productIds, basket.getStoreID(), address, paymentMethod);
        orderRepository.addOrder(order);
        return order.getId();
    }

    @Override
    public int updateOrder(int orderId, Basket basket, Integer userId, String address, PaymentMethod paymentMethod) {
        try {
            List<StoreProductDTO> products = basket.getProducts();
            List<Integer> productIds = products.stream().map(product -> product.getProductId()).toList();
            IOrder updatedOrder = new Order(orderId, userId, OrderState.PENDING, productIds, basket.getStoreID(),
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
    public List<Integer> searchOrders(String keyword) {
        List<Integer> orderIds = new ArrayList<>();
        for (IOrder order : orderRepository.getAllOrders()) {
            if (order.getAddress().contains(keyword) || order.getState().toString().contains(keyword)
                    || order.getPaymentMethod().toString().contains(keyword)
                    || order.getProductIds().toString().contains(keyword)) {
                orderIds.add(order.getId());
            }
        }
        return orderIds;
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

    @Override
    public void addOrderCart(Cart cart, int userId, String address, PaymentMethod paymentMethod) {
        for (Basket basket : cart.getBaskets()) {
            if (basket.getProducts().isEmpty()) {
                logger.error("Basket is empty, cannot create order.");
                throw new IllegalArgumentException("Basket is empty, cannot create order.");
            }// not sopposed to be here, but just in case
            List<Integer> productIds = basket.getProducts().stream()
                .map(StoreProductDTO::getProductId)
                .toList();
            IOrder order = new Order(userId, OrderState.SHIPPED, productIds, basket.getStoreID(), address, paymentMethod);
            orderRepository.addOrder(order);
            logger.info("Order created with ID: {}", order.getId());
        }
      
    }
}