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
public interface UserJpaRepository extends JpaRepository<User, Integer> {
    
    @Query("SELECT r FROM Registered r WHERE r.email = :email")
    Optional<Registered> findRegisteredByEmail(@Param("email") String email);
    
    @Query("SELECT r FROM Registered r")
    List<Registered> findAllRegistered();
    
    @Query("SELECT r FROM Registered r WHERE r.userId = :userId")
    Optional<Registered> findRegisteredById(@Param("userId") int userId);
    
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserId(@Param("userId") int userId);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = User AND u.userId = :userId")
    Optional<User> findGuestById(@Param("userId") int userId);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = User")
    List<User> findAllGuests();
    
    @Query("SELECT r FROM Registered r WHERE r.email LIKE %:keyword% OR CAST(r.age AS string) LIKE %:keyword%")
    List<Registered> searchRegisteredUsers(@Param("keyword") String keyword);
} 