package ApplicationLayer.Services;

import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.UserRepository;

public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    public SystemService() {
        initialize();
    }

    @Override
    public IDelivery getDeliveryService() {
        return deliveryService;
    }

    @Override
    public IAuthenticator getAuthenticatorService() {
        return authenticatorService;
    }

    @Override
    public IPayment getPaymentService() {
        return paymentService;
    }

    @Override
    public void initialize() {
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter();
        this.paymentService = new PaymentAdapter();

        this.userService = new UserService(new UserRepository()); // Assuming you have a UserService implementation
    }

    @Override
    public void shutdown() {
        // Clean up resources if needed
        this.deliveryService = null;
        this.authenticatorService = null;
        this.paymentService = null;
    }

    @Override
    public void GuestLogin(String userName, String Password){
        

    }
}
