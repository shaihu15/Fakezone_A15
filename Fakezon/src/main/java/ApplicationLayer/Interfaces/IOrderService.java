package ApplicationLayer.Interfaces;

import java.util.Collection;
import java.util.List;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Order;

public interface IOrderService {
    int addOrder(Collection<StoreProductDTO> products, int storeId, int userId, String address, PaymentMethod paymentMethod); // change the string To a busket object when the busket class is created
    int updateOrder(int orderId, Collection<Integer> productsIds, int storeID, Integer userId, String address, PaymentMethod paymentMethod); // other parameters can be added as needed
    void deleteOrder(int orderId);
    IOrder viewOrder(int orderId);
    List<IOrder> searchOrders(String keyword);
    int getOrderUserId(int orderId);
    int getOrderStoreId(int orderId);
    List<Integer> getOrderProductIds(int orderId);
    List<IOrder> getOrdersByStoreId(int storeId);
}
