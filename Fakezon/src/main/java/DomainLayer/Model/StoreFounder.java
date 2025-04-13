package DomainLayer.Model;

public class StoreFounder extends RegisteredRole{
    public StoreFounder(Registered registered) {
        super(registered);
    }

    public String getRoleName() {
        return "StoreFounder";
    }
}
