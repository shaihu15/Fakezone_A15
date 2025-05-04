package ApplicationLayer.Interfaces;

import java.util.Collection;
import java.util.List;

import ApplicationLayer.DTO.BasketDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Order;

public interface IOrderService {
    int addOrder(Basket basket, int userId, String address, PaymentMethod paymentMethod);
    int updateOrder(int orderId, Basket basket, Integer userId, String address, PaymentMethod paymentMethod); // other parameters can be added as needed
    void deleteOrder(int orderId);
    IOrder viewOrder(int orderId);
    List<IOrder> searchOrders(String keyword);
    int getOrderUserId(int orderId);
    int getOrderStoreId(int orderId);
    List<Integer> getOrderProductIds(int orderId);
    List<IOrder> getOrdersByStoreId(int storeId);
    void addOrderCart(Cart cart, int userId, String address, PaymentMethod paymentMethod);
}
