package ApplicationLayer.Services;

import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IOrderRepository;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationEventPublisher;

@Configuration
public class AppConfig {
    @Bean
    public IStoreRepository storeRepository() {
        return new StoreRepository();
    }

    @Bean
    public IUserRepository userRepository() {
        return new UserRepository();
    }

    @Bean
    public IProductRepository productRepository() {
        return new ProductRepository();
    }
    @Bean
    public IOrderRepository orderRepository() {
        return new OrderRepository();
    }
    @Bean
    public IUserService userService(IUserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public ISystemService systemService(ApplicationEventPublisher eventPublisher, IStoreRepository storeRepository, IProductRepository productRepository, IUserRepository userRepository, IOrderRepository orderRepository) {
        return new SystemService(storeRepository, userRepository, productRepository,orderRepository, eventPublisher);
    }

    @Bean
    public AuthenticatorAdapter authenticatorAdapter(IUserService userService) {
        return new AuthenticatorAdapter(userService);
    }
}
