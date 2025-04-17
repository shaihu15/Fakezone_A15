package DomainLayer.Model;

import DomainLayer.Enums.RoleName;

public class UnassignedRole extends RegisteredRole {

    public UnassignedRole() {
    }

    public RoleName getRoleName() {
        return RoleName.UNASSIGNED;
    }
}
