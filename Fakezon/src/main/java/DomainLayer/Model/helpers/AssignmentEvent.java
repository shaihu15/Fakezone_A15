package DomainLayer.Model.helpers;

import javax.management.relation.Role;

import DomainLayer.Enums.RoleName;

public class AssignmentEvent {
    private final int storeId;
    private final int userId;
    private final RoleName roleName;

    public AssignmentEvent(int storeId, int userId, RoleName roleName){
        this.storeId = storeId;
        this.userId = userId;
        this.roleName = roleName;
    }

    public int getStoreId(){
        return this.storeId;
    }
    public int getUserId(){
        return this.userId;
    }
    public RoleName getRoleName(){
        return this.roleName;
    }
}
