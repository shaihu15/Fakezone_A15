package DomainLayer.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Offer {
    private int userId;
    private int storeId;
    private int productId;
    private double offerAmount;
    private Set<Integer> ownersAccepted;
    private List<Integer> currentStoreOwners;
    private boolean isApproved = false;
    private boolean isDeclined = false;
    private int declinedBy;

    public Offer(int userId, int storeId, int productId, double offerAmount, List<Integer> currentStoreOwners){
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
        this.offerAmount = offerAmount;
        this.ownersAccepted = new HashSet<>();
        this.currentStoreOwners = currentStoreOwners;
    }

    public int getUserId(){
        return userId;
    }

    public int getStoreId(){
        return storeId;
    }

    public int getProductId(){
        return productId;
    }

    public double getOfferAmount(){
        return offerAmount;
    }

    public Set<Integer> getOwnersAccepted(){
        return ownersAccepted;
    }

    public void approve(int ownerId){
        if(!isDeclined && currentStoreOwners.contains(ownerId)){
            ownersAccepted.add(ownerId);
        }
    }

    public void decline(int ownerId){
        if(!isApproved && currentStoreOwners.contains(ownerId)){
            isDeclined = true;
            declinedBy = ownerId;
        }
    }

    public void removeOwner(Integer ownerId){
        currentStoreOwners.remove(ownerId);
        ownersAccepted.remove(ownerId);
    }

    public boolean isApproved(){
        if(isDeclined){
            return false;
        }
        if(isApproved){
            return isApproved;
        }
        for(Integer owner : currentStoreOwners){
            if(!ownersAccepted.contains(owner)){
                return false;
            }
        }
        isApproved = true;
        return true;
    }

    public boolean isDeclined(){
        return isDeclined;
    }

    public int getDeclinedBy(){
        return declinedBy;
    }
}
