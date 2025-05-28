package DomainLayer.Interfaces;

import java.util.Collection;

public interface IOrderRepository {
        void addOrder(IOrder Order);
        void deleteOrder(int orderId);
        IOrder getOrder(int orderId);
        void clearAllData();

        
        Collection<IOrder> getAllOrders();
        Collection<IOrder> getOrdersByUserId(int userId);

}