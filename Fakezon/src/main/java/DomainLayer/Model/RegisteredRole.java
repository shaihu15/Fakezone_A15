package DomainLayer.Model;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IRegisteredRole;

public abstract class RegisteredRole implements IRegisteredRole {

    public RegisteredRole() {
    }
    public abstract RoleName getRoleName();

}
