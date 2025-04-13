package DomainLayer.Model;

public class StoreOwner  extends RegisteredRole{
    public StoreOwner(Registered registered) {
        super(registered);
    }

    public String getRoleName() {
        return "StoreOwner";
    }

}
