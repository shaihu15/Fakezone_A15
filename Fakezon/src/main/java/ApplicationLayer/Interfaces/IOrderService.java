package ApplicationLayer.Interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.BasketDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Order;

public interface IOrderService {
    void deleteOrder(int orderId);
    IOrder viewOrder(int orderId);
    List<IOrder> searchOrders(String keyword);
    int getOrderUserId(int orderId);
    int getOrderStoreId(int orderId);
    List<Integer> getOrderProductIds(int orderId);
    List<IOrder> getOrdersByStoreId(int storeId);
    void addOrderCart(Map<StoreDTO, Map<StoreProductDTO,Integer>> cart,Map<Integer,Double> prices, int userId, String address, PaymentMethod paymentMethod);
}
