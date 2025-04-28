package DomainLayer.Interfaces;

import java.util.Collection;

public interface IOrderRepository {
        void addOrder(IOrder Order);
        void updateOrder(int orderId, IOrder Order);
        void deleteOrder(int orderId);
        IOrder getOrder(int orderId);
        Collection<IOrder> getAllOrders();

}