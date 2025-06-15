package InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import DomainLayer.Model.Store;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreJpaRepository extends JpaRepository<Store, Integer> {
    
    Optional<Store> findByName(String name);
    
    List<Store> findByIsOpenTrue();
    
    List<Store> findByStoreFounderID(int founderId);
    
    @Query("SELECT s FROM Store s WHERE s.name LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Store> searchByKeyword(@Param("keyword") String keyword);
    
    @Query(value = "SELECT * FROM stores ORDER BY (SELECT AVG(sr.rating) FROM store_ratings sr WHERE sr.store_id = stores.store_id) DESC LIMIT 10", nativeQuery = true)
    List<Store> findTop10StoresByRating();
    
    @Query("SELECT s FROM Store s WHERE :ownerId MEMBER OF s.storeOwners")
    List<Store> findStoresByOwnerId(@Param("ownerId") Integer ownerId);
} 