package DomainLayer.Model;
import DomainLayer.Interfaces.IRegisteredRole;
import javax.management.relation.Role;

public abstract class RegisteredRole implements IRegisteredRole {
    protected Registered registered;

    public RegisteredRole(Registered registered) {
        this.registered = registered;
    }
    public abstract String getRoleName();

}
