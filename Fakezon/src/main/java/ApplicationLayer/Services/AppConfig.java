package ApplicationLayer.Services;

import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IOrderRepository;
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
    public SystemService systemService(ApplicationEventPublisher eventPublisher, IStoreRepository storeRepository, IProductRepository productRepository, IUserRepository userRepository, IOrderRepository orderRepository) {
        return new SystemService(storeRepository, userRepository, productRepository,orderRepository, eventPublisher);
    }
}
