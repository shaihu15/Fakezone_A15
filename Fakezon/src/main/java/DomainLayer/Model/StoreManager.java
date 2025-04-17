package DomainLayer.Model;

import DomainLayer.Enums.RoleName;

public class StoreManager extends RegisteredRole{
    public StoreManager() {
    }

    public RoleName getRoleName() {
        return RoleName.STORE_MANAGER;
    }
}
