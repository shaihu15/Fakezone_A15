package ApplicationLayer.Interfaces;

import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;


public interface IOrderService {
    void deleteOrder(int orderId);
    IOrder viewOrder(int orderId);
    List<IOrder> searchOrders(String keyword);
    int getOrderOrderId(int orderId);
    int getOrderStoreId(int orderId);
    List<IOrder> getOrdersByUserId(int userId);
    List<Integer> getOrderProductIds(int orderId);
    List<IOrder> getOrdersByStoreId(int storeId);
    void addOrderCart(Map<StoreDTO, Map<StoreProductDTO,Boolean>> cart,Map<Integer,Double> prices, int userId, String address, PaymentMethod paymentMethod, int paymentTransactionId, int deliveryTransactionId);
    void clearAllData();
}
