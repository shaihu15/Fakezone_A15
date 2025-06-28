package ApplicationLayer;

import DomainLayer.Model.Registered;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IRegisteredRole;
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
import DomainLayer.Model.helpers.OfferEvents.CounterOfferDeclineEvent;
import DomainLayer.Model.helpers.OfferEvents.CounterOfferEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferAcceptedByAll;
import DomainLayer.Model.helpers.OfferEvents.OfferAcceptedSingleOwnerEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferDeclinedEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferReceivedEvent;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class UserEventListener {

    private final IUserRepository userRepository;
    private final NotificationWebSocketHandler wsHandler; // web socket

    public UserEventListener(IUserRepository userRepository, NotificationWebSocketHandler wsHandler) {
        this.userRepository = userRepository;
        this.wsHandler = wsHandler;
    }

    @Async
    @EventListener
    @Transactional
    public void handleAssignmentEvent(AssignmentEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserId());
        if (user.isPresent()) {
            String msg = "Please approve or decline this role: " + event.getRoleName() + " for store "
                    + event.getStoreId();
            user.get().addAssignmentMessage(
                    new StoreMsg(event.getStoreId(), -1, msg, null, event.getUserId()));
            userRepository.save(user.get());
            if (user.get().isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(event.getUserId()), msg);
            }
        }
    }

    @Async
    @EventListener
    @Transactional

    public void handleClosingStore(ClosingStoreEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getId());
        for (Registered registeredUser : users) {
            String msg = "Store " + event.getId() + " is now closed.";
            registeredUser.addMessageFromStore(new StoreMsg(event.getId(), -1, msg, null, registeredUser.getUserId()));
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        }
    }

    @Async
    @EventListener
    @Transactional

    public void handleResponseFromStore(ResponseFromStoreEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserId());
        user.ifPresent(registeredUser -> {
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), -1, event.getMessage(), null, event.getUserId()));
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), event.getMessage());
            }
        });
    }

    @Async
    @EventListener
    @Transactional

    public void handleAuctionEndedToOwnersEvent(AuctionEndedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "Auction ended for product " + event.getProductID() + ". Highest bid was "
                        + event.getCurrentHighestBid() +
                        " by user " + event.getUserIDHighestBid() + ".";
                registeredUser.addMessageFromStore(
                        new StoreMsg(event.getStoreId(), event.getProductID(), msg, null, registeredUser.getUserId()));
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handleAuctionFailedToOwnersEvent(AuctionFailedToOwnersEvent event) {
        // As requested, fetching all users with roles in the store
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "Auction failed for product " + event.getProductID() + ". Base price was "
                        + event.getBasePrice() + ". " + event.getMessage();
                registeredUser.addMessageFromStore(
                        new StoreMsg(event.getStoreId(), event.getProductID(), msg, null, registeredUser.getUserId()));
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handleApprovedBidOnAuctionEvent(AuctionApprovedBidEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserIDHighestBid()); // Event targets the
                                                                                                    // highest bidder
        user.ifPresent(registeredUser -> {
            String msg = "We are pleased to inform you that your bid has won the auction on product: "
                    + event.getProductID() + ", at a price of: " + event.getCurrentHighestBid()
                    + "! The product has been added to your shopping cart, please purchase it as soon as possible.";
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), event.getProductID(), msg, null, registeredUser.getUserId()));
            registeredUser.addToBasketQuantity(event.getStoreId(), event.getStoreProductDTO().getProductId(), 1);
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleAuctionGotHigherBidEvent(AuctionGotHigherBidEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserIDPrevHighestBid()); // Event targets
                                                                                                        // the
                                                                                                        // previously
                                                                                                        // highest
                                                                                                        // bidder
        user.ifPresent(registeredUser -> {
            String msg = "Your auction bid on product: " + event.getProductID()
                    + " was rejected due to a higher bid of: " + event.getCurrentHighestBid() + ".";
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), event.getProductID(), msg, null, registeredUser.getUserId()));
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleDeclinedBidOnAuctionEvent(AuctionDeclinedBidEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserIDHighestBid()); // Event targets the
                                                                                                    // highest bidder
                                                                                                    // whose bid was
                                                                                                    // declined
        user.ifPresent(registeredUser -> {
            String msg = "We regret to inform you that the offer for product: " + event.getProductID()
                    + " was declined because product is out of store";
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), event.getProductID(), msg, null, registeredUser.getUserId()));
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleOfferReceivedEvent(OfferReceivedEvent event) {
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "Received an offer for product " + event.getProductId() + ". Offer is: $"
                        + event.getOfferAmount() +
                        " by user " + event.getUserId() + " in Store " + event.getStoreId()
                        + ". Please approve or decline this offer.";
                registeredUser.addOfferMessage(new StoreMsg(event.getStoreId(), event.getProductId(), msg,
                        event.getUserId(), registeredUser.getUserId()));
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handleOfferAcceptedSingleOwnerEvent(OfferAcceptedSingleOwnerEvent event) {
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            if (registeredUser.getUserId() != event.getOwnerId()) {
                HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
                if (isOwner(roles, event.getStoreId())) {
                    String msg = "Owner " + event.getOwnerId() + " accepted an offer for product "
                            + event.getProductId() + " in Store " + event.getStoreId() + ". Offer is: $"
                            + event.getOfferAmount() +
                            " by user " + event.getUserId() + ".";
                    registeredUser.addMessageFromStore(new StoreMsg(event.getStoreId(), event.getProductId(), msg, null,
                            registeredUser.getUserId()));
                    userRepository.save(registeredUser);
                    if (registeredUser.isLoggedIn()) {
                        wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                    }
                }
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handleOfferAcceptedByAll(OfferAcceptedByAll event) {
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "An Offer for product " + event.getProductId() + " in Store " + event.getStoreId()
                        + " was accepted by all owners. Offer is: $" + event.getOfferAmount() +
                        " by user " + event.getUserId() + ".";
                registeredUser.addMessageFromStore(
                        new StoreMsg(event.getStoreId(), event.getProductId(), msg, null, registeredUser.getUserId()));
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }

        Optional<Registered> user = userRepository.findRegisteredById(event.getUserId());
        user.ifPresent(registeredUser -> {
            String msg = "We are pleased to inform you that your offer on product: " + event.getProductId()
                    + ", has been approved at a price of: " + event.getOfferAmount()
                    + "! The product has been added to your shopping cart, please purchase it as soon as possible.";
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), event.getProductId(), msg, null, registeredUser.getUserId()));
            registeredUser.addToBasketQuantity(event.getStoreId(), event.getProductId(), 1);
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleOfferDeclinedEvent(OfferDeclinedEvent event) {
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "An Offer for product " + event.getProductId() + " in Store " + event.getStoreId()
                        + " was Declined by " + event.getDeclinedBy() + ". Offer was: $" + event.getOfferAmount() +
                        " by user " + event.getUserId() + ".";

                registeredUser.removeOfferMessage(event.getStoreId(), event.getProductId(), event.getUserId());
                StoreMsg msgObj = new StoreMsg(event.getStoreId(), event.getProductId(), msg, null,
                        registeredUser.getUserId());
                registeredUser.addMessageFromStore(msgObj);
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserId());
        user.ifPresent(registeredUser -> {
            String msg = "We regret to inform you that the offer for product: " + event.getProductId()
                    + " was declined by the store.";
            registeredUser.addMessageFromStore(
                    new StoreMsg(event.getStoreId(), event.getProductId(), msg, null, registeredUser.getUserId()));
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });

    }

    @Async
    @EventListener
    @Transactional
    public void handleCounterOfferEvent(CounterOfferEvent event) {
        Optional<Registered> user = userRepository.findRegisteredById(event.getUserId());
        user.ifPresent(registeredUser -> {
            String msg = "Store " + event.getStoreId() + "'s Owner sent you a Counter Offer for product "
                    + event.getProductId() + ", with amount: $" + event.getOfferAmount();
            StoreMsg storeMsg = new StoreMsg(event.getStoreId(), event.getProductId(), msg, null,
                    registeredUser.getUserId());
            storeMsg.setCounterOffer(); // for ui purposes
            registeredUser.addMessageFromStore(storeMsg);
            userRepository.save(registeredUser);
            if (registeredUser.isLoggedIn()) {
                wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
            }
        });
    }

    @Async
    @EventListener
    @Transactional
    public void handleCounterOfferDeclineEvent(CounterOfferDeclineEvent event) {
        List<Registered> users = userRepository.UsersWithRolesInStoreId(event.getStoreId());
        for (Registered registeredUser : users) {
            HashMap<Integer, IRegisteredRole> roles = registeredUser.getAllRoles();
            if (isOwner(roles, event.getStoreId())) {
                String msg = "User " + event.getUserId() + " Declined your Store's (ID " + event.getStoreId()
                        + ") Counter Offer of $" + event.getOfferAmount() +
                        " on Product " + event.getProductId();
                registeredUser.addMessageFromStore(
                        new StoreMsg(event.getStoreId(), event.getProductId(), msg, null, registeredUser.getUserId()));
                userRepository.save(registeredUser);
                if (registeredUser.isLoggedIn()) {
                    wsHandler.broadcast(String.valueOf(registeredUser.getUserId()), msg);
                }
            }
        }
    }

    private boolean isOwner(HashMap<Integer, IRegisteredRole> roles, int storeId) {
        return roles.containsKey(storeId) && (roles.get(storeId).getRoleName() == RoleName.STORE_OWNER
                || roles.get(storeId).getRoleName() == RoleName.STORE_FOUNDER);
    }

}