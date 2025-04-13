package DomainLayer.Model;

public class StoreManager extends RegisteredRole{
    public StoreManager(Registered registered) {
        super(registered);
    }

    public String getRoleName() {
        return "StoreManager";
    }
}
