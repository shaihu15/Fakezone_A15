package DomainLayer.Model;

import DomainLayer.Enums.RoleName;

public class StoreOwner  extends RegisteredRole{
    public StoreOwner() {
    }

    public RoleName getRoleName() {
        return RoleName.STORE_OWNER;
    }

}
