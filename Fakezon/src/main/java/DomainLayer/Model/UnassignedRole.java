package DomainLayer.Model;

public class UnassignedRole extends RegisteredRole {

    public UnassignedRole(Registered registered) {
        super(registered);
    }

    public String getRoleName() {
        return "Unassigned";
    }
}
