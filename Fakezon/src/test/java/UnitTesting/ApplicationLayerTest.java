package UnitTesting;

import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionGotHigherBidEvent;
import ApplicationLayer.UserEventListener;
import ApplicationLayer.DTO.StoreProductDTO;
import InfrastructureLayer.Repositories.UserRepository;
import java.util.Optional;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ApplicationLayerTest {
    private IUserRepository userRepository;
    private UserEventListener listener;

    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class);
        listener = new UserEventListener(userRepository);
    }


    @Test
    void testResponseSuccess() {
        Response<String> response = new Response<>("data", "msg", true, null, "validToken");
        assertEquals("data", response.getData());
        assertEquals("msg", response.getMessage());
        assertTrue(response.isSuccess());
        assertNull(response.getErrorType());
        assertEquals("validToken", response.getToken());
    }

    @Test
    void testResponseFailureWithErrorType() {
        Response<String> response = new Response<>(null, "fail", false, ErrorType.BAD_REQUEST, null);
        assertNull(response.getData());
        assertEquals("fail", response.getMessage());
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getErrorType());
    }


 
    @Test
    void testHandleAssignmentEvent_UserPresent(){
        AssignmentEvent event = mock(AssignmentEvent.class);
        RoleName roleName = RoleName.STORE_FOUNDER;
        Registered user = mock(Registered.class);
        when(event.getUserId()).thenReturn(1);
        when(event.getStoreId()).thenReturn(2);
        when(event.getRoleName()).thenReturn(roleName);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        listener.handleAssignmentEvent(event);

        verify(user, times(1)).AssignmentMessages(any());
    }

    @Test
    void testHandleAssignmentEvent_UserNotPresent() {
        AssignmentEvent event = mock(AssignmentEvent.class);
        when(event.getUserId()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        listener.handleAssignmentEvent(event);

        // No exception, nothing to verify
    }

    @Test
    void testHandleClosingStore() {
        ClosingStoreEvent event = mock(ClosingStoreEvent.class);
        Registered user1 = mock(Registered.class);
        Registered user2 = mock(Registered.class);
        when(event.getId()).thenReturn(10);
        when(userRepository.UsersWithRolesInStoreId(10)).thenReturn(List.of(user1, user2));
        when(user1.isLoggedIn()).thenReturn(false);
        when(user2.isLoggedIn()).thenReturn(true);

        listener.handleClosingStore(event);

        verify(user1, times(1)).addMessageFromStore(any());
        verify(user2, never()).addMessageFromStore(any());
    }

    @Test
    void testHandleResponseFromStore_UserNotLoggedIn() {
        ResponseFromStoreEvent event = mock(ResponseFromStoreEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserId()).thenReturn(1);
        when(event.getStoreId()).thenReturn(2);
        when(event.getMessage()).thenReturn("msg");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(false);

        listener.handleResponseFromStore(event);

        verify(user, times(1)).addMessageFromStore(any());
    }

    @Test
    void testHandleResponseFromStore_UserLoggedIn() {
        ResponseFromStoreEvent event = mock(ResponseFromStoreEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserId()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(true);

        listener.handleResponseFromStore(event);

        verify(user, never()).addMessageFromStore(any());
    }

    @Test
    void testHandleAuctionEndedToOwnersEvent() {
        AuctionEndedToOwnersEvent event = mock(AuctionEndedToOwnersEvent.class);
        Registered user1 = mock(Registered.class);
        Registered user2 = mock(Registered.class);
        when(event.getStoreId()).thenReturn(5);
        when(event.getProductID()).thenReturn(7);
        when(event.getCurrentHighestBid()).thenReturn(100.0);
        when(event.getUserIDHighestBid()).thenReturn(8);
        when(userRepository.UsersWithRolesInStoreId(5)).thenReturn(List.of(user1, user2));
        when(user1.isLoggedIn()).thenReturn(false);
        when(user2.isLoggedIn()).thenReturn(true);

        listener.handleAuctionEndedToOwnersEvent(event);

        verify(user1, times(1)).addAuctionEndedMessage(any());
        verify(user2, never()).addAuctionEndedMessage(any());
    }

    @Test
    void testHandleAuctionFailedToOwnersEvent() {
        AuctionFailedToOwnersEvent event = mock(AuctionFailedToOwnersEvent.class);
        Registered user = mock(Registered.class);
        when(event.getStoreId()).thenReturn(3);
        when(event.getProductID()).thenReturn(4);
        when(event.getBasePrice()).thenReturn(50.0);
        when(event.getMessage()).thenReturn("fail");
        when(userRepository.UsersWithRolesInStoreId(3)).thenReturn(List.of(user));
        when(user.isLoggedIn()).thenReturn(false);

        listener.handleAuctionFailedToOwnersEvent(event);

        verify(user, times(1)).addMessageFromStore(any());
    }

    @Test
    void testHandleApprovedBidOnAuctionEvent_UserNotLoggedIn() {
        AuctionApprovedBidEvent event = mock(AuctionApprovedBidEvent.class);
        Registered user = mock(Registered.class);
        StoreProductDTO dto = mock(StoreProductDTO.class);
        when(event.getUserIDHighestBid()).thenReturn(1);
        when(event.getStoreId()).thenReturn(2);
        when(event.getProductID()).thenReturn(3);
        when(event.getCurrentHighestBid()).thenReturn(200.0);
        when(event.getStoreProductDTO()).thenReturn(dto);
        when(dto.getProductId()).thenReturn(3);
 
        listener.handleApprovedBidOnAuctionEvent(event);

        verify(user, times(1)).addMessageFromStore(any());
        verify(user, times(1)).addToBasket(2, 3, 1);
    }

    @Test
    void testHandleApprovedBidOnAuctionEvent_UserLoggedIn() {
        AuctionApprovedBidEvent event = mock(AuctionApprovedBidEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserIDHighestBid()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(true);

        listener.handleApprovedBidOnAuctionEvent(event);

        verify(user, never()).addMessageFromStore(any());
        verify(user, never()).addToBasket(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testHandleAuctionGotHigherBidEvent_UserNotLoggedIn() {
        AuctionGotHigherBidEvent event = mock(AuctionGotHigherBidEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserIDPrevHighestBid()).thenReturn(1);
        when(event.getStoreId()).thenReturn(2);
        when(event.getProductID()).thenReturn(3);
        when(event.getCurrentHighestBid()).thenReturn(250.0);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(false);

        listener.handleAuctionGotHigherBidEvent(event);

        verify(user, times(1)).addMessageFromStore(any());
    }

    @Test
    void testHandleAuctionGotHigherBidEvent_UserLoggedIn() {
        AuctionGotHigherBidEvent event = mock(AuctionGotHigherBidEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserIDPrevHighestBid()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(true);

        listener.handleAuctionGotHigherBidEvent(event);

        verify(user, never()).addMessageFromStore(any());
    }

    @Test
    void testHandleDeclinedBidOnAuctionEvent_UserNotLoggedIn() {
        AuctionDeclinedBidEvent event = mock(AuctionDeclinedBidEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserIDHighestBid()).thenReturn(1);
        when(event.getStoreId()).thenReturn(2);
        when(event.getProductID()).thenReturn(3);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(false);

        listener.handleDeclinedBidOnAuctionEvent(event);

        verify(user, times(1)).addMessageFromStore(any());
    }

    @Test
    void testHandleDeclinedBidOnAuctionEvent_UserLoggedIn() {
        AuctionDeclinedBidEvent event = mock(AuctionDeclinedBidEvent.class);
        Registered user = mock(Registered.class);
        when(event.getUserIDHighestBid()).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(user.isLoggedIn()).thenReturn(true);

        listener.handleDeclinedBidOnAuctionEvent(event);

        verify(user, never()).addMessageFromStore(any());
    }
}