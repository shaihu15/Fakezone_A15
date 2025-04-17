package DomainLayer.Model;

public class AuctionProductState implements ProductState {
    private String stateName;

    public AuctionProductState(String stateName) {
        this.stateName = stateName;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

   //add bid method to the auction product state
    public void addBid(double bidAmount) {
        // Logic to add a bid to the auction product
        //System.out.println("Bid of " + bidAmount + " added to the auction product in state: " + stateName);
    }

    //add method to get the current highest bid
    public double getCurrentHighestBid() {
        // Logic to get the current highest bid for the auction product
        return 0.0; // Placeholder value, replace with actual logic
    }

}
