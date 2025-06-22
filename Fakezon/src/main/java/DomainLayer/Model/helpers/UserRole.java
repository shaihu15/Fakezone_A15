package DomainLayer.Model.helpers;

import DomainLayer.Enums.RoleName;
import jakarta.persistence.*;

@Embeddable
public class UserRole {
    
    private int storeId;
    
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    // Default constructor for JPA
    protected UserRole() {
    }

    public UserRole(int storeId, RoleName roleName) {
        this.storeId = storeId;
        this.roleName = roleName;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }
} 