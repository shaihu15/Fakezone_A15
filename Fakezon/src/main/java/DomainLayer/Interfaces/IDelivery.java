package DomainLayer.Interfaces;

public interface IDelivery {
    boolean deliver(String address, String recipient, String packageDetails);
}
