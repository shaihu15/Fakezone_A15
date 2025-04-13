package DomainLayer.Model;
import DomainLayer.IRepository.IRegisteredRole;

public abstract class RegisteredRole implements IRegisteredRole {
    protected Registered registered;

    public RegisteredRole(Registered registered) {
        this.registered = registered;
    }
    public abstract String getRoleName();

}
