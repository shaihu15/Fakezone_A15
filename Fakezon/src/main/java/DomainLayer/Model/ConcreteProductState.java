package DomainLayer.Model;

public class ConcreteProductState implements ProductState {
    private String stateName;

    public ConcreteProductState(String stateName) {
        this.stateName = stateName;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

}
