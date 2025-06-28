package InfrastructureLayer.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import DomainLayer.Model.Order;
import DomainLayer.Enums.OrderState;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Integer> {
    
    List<Order> findByUserId(int userId);
    
    List<Order> findByStoreId(int storeId);
    
    List<Order> findByOrderState(OrderState orderState);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.storeId = :storeId")
    List<Order> findByUserIdAndStoreId(@Param("userId") int userId, @Param("storeId") int storeId);
    
    @Query("SELECT o FROM Order o WHERE o.address LIKE %:keyword% OR " +
           "CAST(o.orderState AS string) LIKE %:keyword% OR " +
           "CAST(o.paymentMethod AS string) LIKE %:keyword%")
    List<Order> searchOrdersByKeyword(@Param("keyword") String keyword);
} 