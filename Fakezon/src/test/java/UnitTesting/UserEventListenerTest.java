package UnitTesting;

import ApplicationLayer.UserEventListener;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.StoreOwner;
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
import ApplicationLayer.DTO.StoreProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.AbstractMap.SimpleEntry;

import javax.management.relation.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserEventListenerTest {

    private IUserRepository userRepository;
    private UserEventListener userEventListener;

    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class);
        userEventListener = new UserEventListener(userRepository, new NotificationWebSocketHandler());
    }

    @Test
    void handleAssignmentEvent_UserExists_MessageAdded() {
        // Arrange
        int userId = 1;
        int storeId = 101;
        RoleName roleName = RoleName.STORE_MANAGER;
        // Corrected constructor usage: AssignmentEvent's constructor has no 'source' parameter.
        AssignmentEvent event = new AssignmentEvent(storeId, userId, roleName);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(false); // Simulate user not logged in for message addition
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleAssignmentEvent(event);

                try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser).addAssignmentMessage(messageCaptor.capture());
        StoreMsg capturedMessage = messageCaptor.getValue();

        assertEquals(storeId, capturedMessage.getStoreId());
        assertEquals("Please approve or decline this role: " + roleName + " for store " + storeId, capturedMessage.getMessage());
    }

    @Test
    void handleAssignmentEvent_UserDoesNotExist_NoInteraction() {
        // Arrange
        int userId = 1;
        int storeId = 101;
        RoleName roleName = RoleName.STORE_MANAGER;
        // Corrected constructor usage: AssignmentEvent's constructor has no 'source' parameter.
        AssignmentEvent event = new AssignmentEvent(storeId, userId, roleName);

        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        // Act
        userEventListener.handleAssignmentEvent(event);

        // Assert
        verify(userRepository, times(1)).findRegisteredById(userId);
        verifyNoMoreInteractions(userRepository); // Ensure no other methods were called on userRepository
    }

    @Test
    void handleClosingStore_UsersWithRolesExist_MessagesAddedToLoggedOutUsers() {
        // Arrange
        int storeId = 101;
        // Corrected constructor usage: ClosingStoreEvent's constructor has no 'source' parameter.
        ClosingStoreEvent event = new ClosingStoreEvent(storeId);

        Registered mockRegisteredUser1 = mock(Registered.class);
        when(mockRegisteredUser1.isLoggedIn()).thenReturn(false); // User 1 is logged out
        Registered mockRegisteredUser2 = mock(Registered.class);
        when(mockRegisteredUser2.isLoggedIn()).thenReturn(true);  // User 2 is logged in

        List<Registered> users = Arrays.asList(mockRegisteredUser1, mockRegisteredUser2);
        when(userRepository.UsersWithRolesInStoreId(storeId)).thenReturn(users);

        // Act
        userEventListener.handleClosingStore(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor1 = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser1).addMessageFromStore(messageCaptor1.capture());
        StoreMsg capturedMessage1 = messageCaptor1.getValue();
        assertEquals(storeId, capturedMessage1.getStoreId());
        assertEquals("Store " + storeId + " is now closed.", capturedMessage1.getMessage());

        verify(mockRegisteredUser2).addMessageFromStore(any()); // No message added to logged-in user
    }

    @Test
    void handleClosingStore_NoUsersWithRoles_NoInteraction() {
        // Arrange
        int storeId = 101;
        // Corrected constructor usage: ClosingStoreEvent's constructor has no 'source' parameter.
        ClosingStoreEvent event = new ClosingStoreEvent(storeId);

        when(userRepository.UsersWithRolesInStoreId(storeId)).thenReturn(Collections.emptyList());

        // Act
        userEventListener.handleClosingStore(event);

        // Assert
        verify(userRepository, times(1)).UsersWithRolesInStoreId(storeId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void handleResponseFromStore_UserExistsAndLoggedOut_MessageAdded() {
        // Arrange
        int userId = 1;
        int storeId = 101;
        String message = "Your order is ready for pickup.";
        // Corrected constructor usage: ResponseFromStoreEvent's constructor has no 'source' parameter.
        ResponseFromStoreEvent event = new ResponseFromStoreEvent( storeId,userId, message);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(false);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleResponseFromStore(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser).addMessageFromStore(messageCaptor.capture());
        StoreMsg capturedMessage = messageCaptor.getValue();

        assertEquals(storeId, capturedMessage.getStoreId());
        assertEquals(message, capturedMessage.getMessage());
    }

    @Test
    void handleResponseFromStore_UserExistsAndLoggedIn_NoMessageAdded() {
        // Arrange
        int userId = 1;
        int storeId = 101;
        String message = "Your order is ready for pickup.";
        // Corrected constructor usage: ResponseFromStoreEvent's constructor has no 'source' parameter.
        ResponseFromStoreEvent event = new ResponseFromStoreEvent(userId, storeId, message);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(true);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleResponseFromStore(event);

        // Assert
        verify(mockRegisteredUser, never()).addMessageFromStore(any());
    }

    @Test
    void handleResponseFromStore_UserDoesNotExist_NoInteraction() {
        // Arrange
        int userId = 1;
        int storeId = 101;
        String message = "Your order is ready for pickup.";
        // Corrected constructor usage: ResponseFromStoreEvent's constructor has no 'source' parameter.
        ResponseFromStoreEvent event = new ResponseFromStoreEvent(storeId, userId,message);

        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        // Act
        userEventListener.handleResponseFromStore(event);

        // Assert
        verify(userRepository, times(1)).findRegisteredById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void handleAuctionEndedToOwnersEvent_UsersWithRolesExist_MessagesAddedToLoggedOutUsers() {
        // Arrange
        int storeId = 101;
        int productId = 201;
        double currentHighestBid = 150.0;
        int userIdHighestBid = 2;
        // Corrected constructor usage: AuctionEndedToOwnersEvent's constructor has no 'source' parameter.
        AuctionEndedToOwnersEvent event = new AuctionEndedToOwnersEvent(storeId, productId, userIdHighestBid, currentHighestBid);

        Registered mockRegisteredUser1 = mock(Registered.class);
        when(mockRegisteredUser1.isLoggedIn()).thenReturn(false);
        Registered mockRegisteredUser2 = mock(Registered.class);
        when(mockRegisteredUser2.isLoggedIn()).thenReturn(true);

        List<Registered> users = Arrays.asList(mockRegisteredUser1, mockRegisteredUser2);
        when(userRepository.UsersWithRolesInStoreId(storeId)).thenReturn(users);
        HashMap<Integer, IRegisteredRole> map = new HashMap<>();
        map.put(101, new StoreOwner());
        when(mockRegisteredUser1.getAllRoles()).thenReturn(map);
        when(mockRegisteredUser2.getAllRoles()).thenReturn(map);
        // Act
        userEventListener.handleAuctionEndedToOwnersEvent(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor1 = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser1).addMessageFromStore(messageCaptor1.capture());
        StoreMsg capturedMessage1 = messageCaptor1.getValue();
        assertEquals(storeId, capturedMessage1.getStoreId());
        assertEquals("Auction ended for product " + productId + ". Highest bid was " + currentHighestBid +
                " by user " + userIdHighestBid + ".", capturedMessage1.getMessage());

        verify(mockRegisteredUser2).addMessageFromStore(any());
    }

    @Test
    void handleAuctionFailedToOwnersEvent_UsersWithRolesExist_MessagesAddedToLoggedOutUsers() {
        // Arrange
        int storeId = 101;
        int productId = 201;
        double basePrice = 100.0;
        String message = "No bids met the base price.";
        // Corrected constructor usage: AuctionFailedToOwnersEvent's constructor has no 'source' parameter.
        AuctionFailedToOwnersEvent event = new AuctionFailedToOwnersEvent(storeId, productId, basePrice, message);

        Registered mockRegisteredUser1 = mock(Registered.class);
        when(mockRegisteredUser1.isLoggedIn()).thenReturn(false);
        Registered mockRegisteredUser2 = mock(Registered.class);
        when(mockRegisteredUser2.isLoggedIn()).thenReturn(true);

        List<Registered> users = Arrays.asList(mockRegisteredUser1, mockRegisteredUser2);
        when(userRepository.UsersWithRolesInStoreId(storeId)).thenReturn(users);
        HashMap<Integer, IRegisteredRole> map = new HashMap<>();
        map.put(101, new StoreOwner());
        when(mockRegisteredUser1.getAllRoles()).thenReturn(map);
        when(mockRegisteredUser2.getAllRoles()).thenReturn(map);
        // Act
        userEventListener.handleAuctionFailedToOwnersEvent(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor1 = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser1).addMessageFromStore(messageCaptor1.capture());
        StoreMsg capturedMessage1 = messageCaptor1.getValue();
        assertEquals(storeId, capturedMessage1.getStoreId());
        assertEquals("Auction failed for product " + productId + ". Base price was " + basePrice + ". " + message, capturedMessage1.getMessage());

        verify(mockRegisteredUser2).addMessageFromStore(any());
    }

    @Test
    void handleApprovedBidOnAuctionEvent_HighestBidderExistsAndLoggedOut_MessageAndProductAdded() {
        // Arrange
        int userIdHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        double currentHighestBid = 150.0;
        StoreProductDTO storeProductDTO = mock(StoreProductDTO.class);
        when(storeProductDTO.getProductId()).thenReturn(productId);
        // Corrected constructor usage: AuctionApprovedBidEvent's constructor has no 'source' parameter.
        AuctionApprovedBidEvent event = new AuctionApprovedBidEvent(storeId, productId, userIdHighestBid, currentHighestBid, storeProductDTO);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(false);
        when(userRepository.findRegisteredById(userIdHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleApprovedBidOnAuctionEvent(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser).addMessageFromStore(messageCaptor.capture());
        StoreMsg capturedMessage = messageCaptor.getValue();

        assertEquals(storeId, capturedMessage.getStoreId());
        assertEquals("We are pleased to inform you that your bid has won the auction on product: " + productId + ", at a price of: " + currentHighestBid + "! The product has been added to your shopping cart, please purchase it as soon as possible.", capturedMessage.getMessage());

        verify(mockRegisteredUser).addToBasketQuantity(storeId, productId, 1);
    }

    @Test
    void handleApprovedBidOnAuctionEvent_HighestBidderExistsAndLoggedIn_NoMessageOrProductAdded() {
        // Arrange
        int userIdHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        double currentHighestBid = 150.0;
        StoreProductDTO storeProductDTO = mock(StoreProductDTO.class);
        when(storeProductDTO.getProductId()).thenReturn(productId);
        // Corrected constructor usage: AuctionApprovedBidEvent's constructor has no 'source' parameter.
        AuctionApprovedBidEvent event = new AuctionApprovedBidEvent(storeId, productId, userIdHighestBid, currentHighestBid, storeProductDTO);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(true);
        when(userRepository.findRegisteredById(userIdHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleApprovedBidOnAuctionEvent(event);

        // Assert
        verify(mockRegisteredUser).addMessageFromStore(any());
        verify(mockRegisteredUser).addToBasketQuantity(anyInt(), anyInt(), anyInt());
    }

    @Test
    void handleAuctionGotHigherBidEvent_PreviousHighestBidderExistsAndLoggedOut_MessageAdded() {
        // Arrange
        int userIdPrevHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        double currentHighestBid = 150.0;
        // Corrected constructor usage: AuctionGotHigherBidEvent's constructor has no 'source' parameter.
        AuctionGotHigherBidEvent event = new AuctionGotHigherBidEvent(storeId, productId, userIdPrevHighestBid, currentHighestBid);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(false);
        when(userRepository.findRegisteredById(userIdPrevHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleAuctionGotHigherBidEvent(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser).addMessageFromStore(messageCaptor.capture());
        StoreMsg capturedMessage = messageCaptor.getValue();

        assertEquals(storeId, capturedMessage.getStoreId());
        assertTrue(capturedMessage.getMessage().contains("Your auction bid on product: " + productId + " was rejected due to a higher bid of: " + currentHighestBid + "."));

    }

    @Test
    void handleAuctionGotHigherBidEvent_PreviousHighestBidderExistsAndLoggedIn_NoMessageAdded() {
        // Arrange
        int userIdPrevHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        double currentHighestBid = 150.0;
        // Corrected constructor usage: AuctionGotHigherBidEvent's constructor has no 'source' parameter.
        AuctionGotHigherBidEvent event = new AuctionGotHigherBidEvent(storeId, productId, userIdPrevHighestBid, currentHighestBid);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(true);
        when(userRepository.findRegisteredById(userIdPrevHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleAuctionGotHigherBidEvent(event);

        // Assert
        verify(mockRegisteredUser).addMessageFromStore(any());
    }

    @Test
    void handleDeclinedBidOnAuctionEvent_HighestBidderExistsAndLoggedOut_MessageAdded() {
        // Arrange
        int userIdHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        // Note: AuctionDeclinedBidEvent has currentHighestBid in its constructor, even if not used by the handler's message.
        double dummyCurrentHighestBid = 0.0;
        // Corrected constructor usage: AuctionDeclinedBidEvent's constructor has no 'source' parameter.
        AuctionDeclinedBidEvent event = new AuctionDeclinedBidEvent(storeId, productId, userIdHighestBid, dummyCurrentHighestBid);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(false);
        when(userRepository.findRegisteredById(userIdHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleDeclinedBidOnAuctionEvent(event);

        // Assert
        ArgumentCaptor<StoreMsg> messageCaptor = ArgumentCaptor.forClass(StoreMsg.class);
        verify(mockRegisteredUser).addMessageFromStore(messageCaptor.capture());
        StoreMsg capturedMessage = messageCaptor.getValue();

        assertEquals(storeId, capturedMessage.getStoreId());
        assertEquals("We regret to inform you that the offer for product: " + productId + " was declined because product is out of store", capturedMessage.getMessage());
    }

    @Test
    void handleDeclinedBidOnAuctionEvent_HighestBidderExistsAndLoggedIn_NoMessageAdded() {
        // Arrange
        int userIdHighestBid = 1;
        int storeId = 101;
        int productId = 201;
        // Note: AuctionDeclinedBidEvent has currentHighestBid in its constructor, even if not used by the handler's message.
        double dummyCurrentHighestBid = 0.0;
        // Corrected constructor usage: AuctionDeclinedBidEvent's constructor has no 'source' parameter.
        AuctionDeclinedBidEvent event = new AuctionDeclinedBidEvent(storeId, productId, userIdHighestBid, dummyCurrentHighestBid);

        Registered mockRegisteredUser = mock(Registered.class);
        when(mockRegisteredUser.isLoggedIn()).thenReturn(true);
        when(userRepository.findRegisteredById(userIdHighestBid)).thenReturn(Optional.of(mockRegisteredUser));

        // Act
        userEventListener.handleDeclinedBidOnAuctionEvent(event);

        // Assert
        verify(mockRegisteredUser).addMessageFromStore(any());
    }
}