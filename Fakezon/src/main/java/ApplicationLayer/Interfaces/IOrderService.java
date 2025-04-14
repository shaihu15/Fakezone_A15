package ApplicationLayer.Interfaces;

import java.util.List;

public interface IOrderService {
    int addOrder(String busket); // change the string To a busket object when the busket class is created
    int updateOrder(int orderId, int productId, int quantity); // other parameters can be added as needed
    void deleteOrder(int orderId);
    String viewOrder(int orderId); // TODO: when the IOrderDTO interface is created, change the return type to IOrderDTO
    List<String> searchOrders(String keyword);// TODO: when the IOrderDTO interface is created, change the return type to List<IOrderDTO>
}
