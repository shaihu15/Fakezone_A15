package DomainLayer.Model;

public class SystemAdministrator extends RegisteredRole {

    public SystemAdministrator(Registered registered) {
        super(registered);
    }

    public String getRoleName() {
        return "SystemAdministrator";
    }
}


