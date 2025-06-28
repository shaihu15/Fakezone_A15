package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "auction_products")
public class AuctionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Column(nullable = false)
    private double currentHighestBid;

    @Column(nullable = false)
    private int minutesToEnd;

    @Column(nullable = false)
    private int userIDHighestBid;

    @Column(nullable = false)
    private boolean isDone;

    // Default constructor for JPA
    public AuctionProduct() {}

    public AuctionProduct(StoreProduct storeProduct, double basePrice, int minutesToEnd) {
        this.storeProduct = storeProduct;
        this.currentHighestBid = basePrice;
        this.minutesToEnd = minutesToEnd;
        this.userIDHighestBid = -1; // No bids yet
        this.isDone = false;
    }

    public int getId() {
        return id;
    }

    public StoreProduct getStoreProduct() {
        return storeProduct;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public int getMinutesToEnd() {
        return minutesToEnd;
    }

    public int getUserIDHighestBid() {
        if (userIDHighestBid < 1) {
            return -1; // No bids yet, return -1
        }
        return userIDHighestBid;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public synchronized int addBid(int userID, double bidAmount) {
        int prev = userID;
        if (bidAmount > currentHighestBid) {
            prev = getUserIDHighestBid();
            currentHighestBid = bidAmount;
            userIDHighestBid = userID;
        }
        return prev;
    }

    public void addMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Added minutes must be greater than 0");
        }
        minutesToEnd += minutes;
    }

    public StoreProductDTO toDTO(int storeId) {
        return new StoreProductDTO(this.getStoreProduct().getSproductID(), this.getStoreProduct().getName(), this.getCurrentHighestBid(), this.getStoreProduct().getQuantity(),
                this.getStoreProduct().getAverageRating(), storeId, this.getStoreProduct().getCategory()); 
    }
}

