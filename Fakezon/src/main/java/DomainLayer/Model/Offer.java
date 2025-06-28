package DomainLayer.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import jakarta.persistence.*;

@Entity
@Table(name = "offers")
public class Offer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;
    
    @Column(name = "user_id", nullable = false)
    private int userId;
    
    @Column(name = "store_id", nullable = false)
    private int storeId;
    
    @Column(name = "product_id", nullable = false)
    private int productId;
    
    @Column(name = "offer_amount", nullable = false)
    private double offerAmount;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "offer_owners_accepted", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "owner_id")
    private Set<Integer> ownersAccepted = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "offer_current_store_owners", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "owner_id")
    private List<Integer> currentStoreOwners = new ArrayList<>();
    
    @Column(name = "is_approved", nullable = false)
    private boolean isApproved = false;
    
    @Column(name = "is_declined", nullable = false)
    private boolean isDeclined = false;
    
    @Column(name = "declined_by")
    private int declinedBy;
    
    @Column(name = "is_handled", nullable = false)
    private boolean isHandled = false;
    
    @Column(name = "offer_type", nullable = false)
    private String offerType = "REGULAR"; // "REGULAR" or "PENDING"

    // Default constructor for JPA
    public Offer() {
        this.ownersAccepted = new HashSet<>();
        this.currentStoreOwners = new ArrayList<>();
    }

    public Offer(int userId, int storeId, int productId, double offerAmount, List<Integer> currentStoreOwners){
        this();
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
        this.offerAmount = offerAmount;
        this.currentStoreOwners = new ArrayList<>(currentStoreOwners);
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

    public boolean isHandled(){
        return isHandled;
    }

    public void setHandled(){
        isHandled = true;
    }
    
    // Additional getters/setters for JPA
    public Long getOfferId() {
        return offerId;
    }
    
    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public void setOfferAmount(double offerAmount) {
        this.offerAmount = offerAmount;
    }
    
    public void setOwnersAccepted(Set<Integer> ownersAccepted) {
        this.ownersAccepted = ownersAccepted;
    }
    
    public List<Integer> getCurrentStoreOwners() {
        return currentStoreOwners;
    }
    
    public void setCurrentStoreOwners(List<Integer> currentStoreOwners) {
        this.currentStoreOwners = currentStoreOwners;
    }
    
    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public void setDeclined(boolean isDeclined) {
        this.isDeclined = isDeclined;
    }
    
    public void setDeclinedBy(int declinedBy) {
        this.declinedBy = declinedBy;
    }
    
    public void setHandled(boolean isHandled) {
        this.isHandled = isHandled;
    }
    
    public String getOfferType() {
        return offerType;
    }
    
    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }
}
