package DomainLayer.Interfaces;

public interface IOrderRepository {
        void addOrder(String Order); // TODO: change the string to a IOrder object when the IOrder class is created
        void updateOrder(int orderId, String Order); // TODO: change the string to a IOrder object when the IOrder class is created
        void deleteOrder(int orderId);
        String getOrder(int orderId); // TODO: when the IOrder interface is created, change the return type to IOrder
}