package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@Table(name = "discount_scopes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "scope_type", discriminatorType = DiscriminatorType.STRING)
public abstract class BaseDiscountScope implements IDiscountScope {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scope_id")
    private Long id;
    
    @Column(name = "store_id")
    private int storeId;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Default constructor for JPA
    protected BaseDiscountScope() {
    }
    
    public BaseDiscountScope(int storeId) {
        this.storeId = storeId;
    }
    
    public Long getId() {
        return id;
    }
    
    public int getStoreId() {
        return storeId;
    }
    
    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
} 