package DomainLayer.Interfaces;

public interface IDelivery {
    boolean deliver(String country, String address, String recipient, String packageDetails);
}
