package ApplicationLayer.Services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.DTO.BasketDTO;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.StoreProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Model.Cart;
import ApplicationLayer.Interfaces.IOrderService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Order;
import DomainLayer.Model.OrderedProduct;

public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    protected static final AtomicInteger idCounter = new AtomicInteger(0);

    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;

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

    @Override
    public void addOrderCart(Map<StoreDTO, Map<StoreProductDTO,Boolean>> cart,Map<Integer,Double> prices, int userId, String address, PaymentMethod paymentMethod) {
        try {
            List<OrderedProduct> orderedProducts = new ArrayList<>();
            for (Map.Entry<StoreDTO, Map<StoreProductDTO,Boolean>> entry : cart.entrySet()) {
                StoreDTO store = entry.getKey();
                Map<StoreProductDTO,Boolean> products = entry.getValue();
                for (Map.Entry<StoreProductDTO,Boolean> productEntry : products.entrySet()) {
                    StoreProductDTO storeProduct = productEntry.getKey();
                    int quantity = storeProduct.getQuantity();
                    orderedProducts.add(new OrderedProduct(storeProduct, quantity));
                }
                double price = prices.get(store.getStoreId());
                Order order = new Order(idCounter.incrementAndGet(), store.getStoreId(), userId, OrderState.SHIPPED, orderedProducts, address, paymentMethod, price);
                orderRepository.addOrder(order);
            }

        } catch (IllegalArgumentException e) {
            logger.error("While trying to add order, recived error {}", e);
            throw e;
        }
    }
}