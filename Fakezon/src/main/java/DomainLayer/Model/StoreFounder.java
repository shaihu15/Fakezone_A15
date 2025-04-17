package DomainLayer.Model;

import DomainLayer.Enums.RoleName;

public class StoreFounder extends RegisteredRole{
    public StoreFounder() {
    }

    public RoleName getRoleName() {
        return RoleName.STORE_FOUNDER;
    }
}
