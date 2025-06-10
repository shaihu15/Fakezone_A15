package DomainLayer.Interfaces;

public interface IDelivery {
    int deliver(String country, String address, String recipient, String packageDetails);
}
