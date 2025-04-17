package DomainLayer.Model;

import DomainLayer.Enums.RoleName;

public class SystemAdministrator extends RegisteredRole {

    public SystemAdministrator() {;
    }

    public RoleName getRoleName() {
        return RoleName.SYSTEM_ADMINISTRATOR;
    }
}


