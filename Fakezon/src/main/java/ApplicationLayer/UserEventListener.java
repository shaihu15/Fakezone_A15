package ApplicationLayer;

import DomainLayer.Model.Registered;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionGotHigherBidEvent;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;

@Component
public class UserEventListener {

    private final IUserRepository userRepository;
    private final NotificationWebSocketHandler wsHandler; //web socket

    public UserEventListener(IUserRepository userRepository, NotificationWebSocketHandler wsHandler) {
        this.userRepository = userRepository;
        this.wsHandler = wsHandler;
    }

    @Async
    @EventListener
    public void handleAssignmentEvent(AssignmentEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserId());
        if (user.isPresent()) {
            String msg = "Please approve or decline this role: " + event.getRoleName() + " for store " + event.getStoreId();
            user.get().addAssignmentMessage(
                new StoreMsg(event.getStoreId(), -1, msg)
            );
            if(user.get().isLoggedIn()){
                wsHandler.broadcast(String.valueOf(event.getUserId()), msg);
            }
        }
    }

    @Async
    @EventListener
    public void handleClosingStore(ClosingStoreEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getId());
        for (Registered registeredUser : users) {
            String msg = "Store " + event.getId() + " is now closed.";
            registeredUser.addMessageFromStore(new StoreMsg(event.getId(), -1, msg));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        }
    }
    @Async
    @EventListener
    public void handleResponseFromStore(ResponseFromStoreEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserId());
        user.ifPresent(registeredUser -> {
            registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), -1, event.getMessage()));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), event.getMessage());
            }
        });
    }

    @Async
    @EventListener
    public void handleAuctionEndedToOwnersEvent(AuctionEndedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            String msg =   "Auction ended for product " + event.getProductID() + ". Highest bid was " + event.getCurrentHighestBid() +
                            " by user " + event.getUserIDHighestBid() + ". Please approve or decline this bid.";
            registeredUser.addAuctionEndedMessage(new StoreMsg(event.getStoreId(), event.getProductID(), msg));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        }
    }

    @Async
    @EventListener
    public void handleAuctionFailedToOwnersEvent(AuctionFailedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            String msg = "Auction failed for product " + event.getProductID() + ". Base price was " + event.getBasePrice() + ". " + event.getMessage();
            registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), event.getProductID(), msg));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        }
    }

    @Async
    @EventListener
    public void handleApprovedBidOnAuctionEvent(AuctionApprovedBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDHighestBid()); // Event targets the highest bidder
        user.ifPresent(registeredUser -> {
            String msg = "We are pleased to inform you that your bid has won the auction on product: " + event.getProductID() + ", at a price of: " + event.getCurrentHighestBid() + "! The product has been added to your shopping cart, please purchase it as soon as possible.";
            registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), event.getProductID(), msg));
            registeredUser.addToBasket(event.getStoreId(), event.getStoreProductDTO().getProductId(), 1);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    public void handleAuctionGotHigherBidEvent(AuctionGotHigherBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDPrevHighestBid()); // Event targets the previously highest bidder
        user.ifPresent(registeredUser -> {
            String msg = "Your auction bid on product: " + event.getProductID() + " was rejected due to a higher bid of: " + event.getCurrentHighestBid() + ".";
            registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), event.getProductID(), msg));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    public void handleDeclinedBidOnAuctionEvent(AuctionDeclinedBidEvent event) {
        Optional<Registered> user = userRepository.findById(event.getUserIDHighestBid()); // Event targets the highest bidder whose bid was declined
        user.ifPresent(registeredUser -> {
            String msg = "We regret to inform you that the offer for product: " + event.getProductID() + " was not approved by the store.";
            registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), event.getProductID(), msg));
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }
}