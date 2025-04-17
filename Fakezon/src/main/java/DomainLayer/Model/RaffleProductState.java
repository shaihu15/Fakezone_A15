package DomainLayer.Model;

public class RaffleProductState implements ProductState {
    private String stateName;

    public RaffleProductState(String stateName) {
        this.stateName = stateName;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

    // add raffle method to the raffle product state
    public void addRaffle(double raffleAmount) {
        // Logic to add a raffle to the raffle product
        // System.out.println("Raffle of " + raffleAmount + " added to the raffle product in state: " + stateName);
    }

}
