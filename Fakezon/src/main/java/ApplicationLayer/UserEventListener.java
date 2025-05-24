package ApplicationLayer;

import DomainLayer.Model.Registered;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionGotHigherBidEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

@Component
public class UserEventListener {

    private final IUserRepository userRepository;

    public UserEventListener(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Async
    @EventListener
    public void handleAssignmentEvent(AssignmentEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserId());
        if (user.isPresent()) {
            user.get().AssignmentMessages(
                new SimpleEntry<>(event.getStoreId(),
                "Please approve or decline this role: " + event.getRoleName() +
                " for store " + event.getStoreId())
            );
        }
    }

    @Async
    @EventListener
    public void handleClosingStore(ClosingStoreEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getId());
        for (Registered registeredUser : users) {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getId(), "Store " + event.getId() + " is now closed."));
            }
            // Here would be logic to send to UI if user is logged in
        }
    }
    @Async
    @EventListener
    public void handleResponseFromStore(ResponseFromStoreEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserId());
        user.ifPresent(registeredUser -> {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getStoreId(), event.getMessage()));
            }
            // Here would be logic to send to UI if user is logged in
        });
    }

    @Async
    @EventListener
    public void handleAuctionEndedToOwnersEvent(AuctionEndedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addAuctionEndedMessage(new SimpleEntry<>(event.getStoreId(),
                "Auction ended for product " + event.getProductID() + ". Highest bid was " + event.getCurrentHighestBid() +
                " by user " + event.getUserIDHighestBid() + ". Please approve or decline this bid."));
            }
            // Here would be logic to send to UI if user is logged in
        }
    }

    @Async
    @EventListener
    public void handleAuctionFailedToOwnersEvent(AuctionFailedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getStoreId(),
                "Auction failed for product " + event.getProductID() + ". Base price was " + event.getBasePrice() + ". " + event.getMessage()));
            }
            // Here would be logic to send to UI if user is logged in
        }
    }

    @Async
    @EventListener
    public void handleApprovedBidOnAuctionEvent(AuctionApprovedBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDHighestBid()); // Event targets the highest bidder
        user.ifPresent(registeredUser -> {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getStoreId(),
                "We are pleased to inform you that your bid has won the auction on product: " + event.getProductID() + ", at a price of: " + event.getCurrentHighestBid() + "! The product has been added to your shopping cart, please purchase it as soon as possible."));

                // Add product to user's basket (assuming Registered has an addToBasket method)
                registeredUser.addToBasket(event.getStoreId(), event.getStoreProductDTO().getProductId(), 1);
            }
            // Here would be logic to send to UI if user is logged in
        });
    }

    @Async
    @EventListener
    public void handleAuctionGotHigherBidEvent(AuctionGotHigherBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDPrevHighestBid()); // Event targets the previously highest bidder
        user.ifPresent(registeredUser -> {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getStoreId(),
                "Your auction bid on product: " + event.getProductID() + " was rejected due to a higher bid of: " + event.getCurrentHighestBid() + "."));
            }
            // Here would be logic to send to UI if user is logged in
        });
    }

    @Async
    @EventListener
    public void handleDeclinedBidOnAuctionEvent(AuctionDeclinedBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDHighestBid()); // Event targets the highest bidder whose bid was declined
        user.ifPresent(registeredUser -> {
            // Only add message if user is not logged in (as per original logic intent)
            if (!registeredUser.isLoggedIn()) {
                registeredUser.addMessageFromStore(new SimpleEntry<>(event.getStoreId(),
                "We regret to inform you that the offer for product: " + event.getProductID() + " was not approved by the store."));
            }
            // Here would be logic to send to UI if user is logged in
        });
    }
}