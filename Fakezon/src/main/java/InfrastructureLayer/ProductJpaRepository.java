package InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Model.Product;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Integer> {
    
    List<Product> findByCategory(PCategory category);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p JOIN p.storesIds s WHERE s = :storeId")
    List<Product> findByStoreId(@Param("storeId") Integer storeId);
} 