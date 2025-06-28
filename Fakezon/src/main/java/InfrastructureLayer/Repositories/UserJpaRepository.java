package InfrastructureLayer.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import DomainLayer.Model.User;
import DomainLayer.Model.Registered;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<Registered, Integer> {
    
    @Query("SELECT r FROM Registered r WHERE r.email = :email")
    Optional<Registered> findRegisteredByEmail(@Param("email") String email);
    
    @Query("SELECT r FROM Registered r")
    List<Registered> findAllRegistered();
    
    @Query("SELECT r FROM Registered r WHERE r.userId = :userId")
    Optional<Registered> findRegisteredById(@Param("userId") int userId);
    
    @Query("SELECT r FROM Registered r WHERE r.email LIKE %:keyword% OR CAST(r.age AS string) LIKE %:keyword%")
    List<Registered> searchRegisteredUsers(@Param("keyword") String keyword);
    
    @Query("SELECT COALESCE(MAX(r.userId), 0) FROM Registered r")
    Integer findMaxRegisteredUserId();
} 