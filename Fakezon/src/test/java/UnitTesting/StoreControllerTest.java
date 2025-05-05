package UnitTesting;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import com.fakezone.fakezone.controller.StoreController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreControllerTest {

    @Mock
    private ISystemService systemService;

    @Mock
    private AuthenticatorAdapter authenticatorAdapter;

    @InjectMocks
    private StoreController storeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddStore_Success() {
        int userId = 1;
        String storeName = "Test Store";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStore(userId, storeName)).thenReturn(new Response<>(null, "Store added successfully", true));

        ResponseEntity<Response<Void>> response = storeController.addStore(userId, storeName, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addStore(userId, storeName);
    }

    @Test
    void testAddStore_InvalidToken() {
        int userId = 1;
        String storeName = "Test Store";
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.addStore(userId, storeName, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).addStore(anyInt(), anyString());
    }




    @Test
    void testGetAllStoreOrders_InvalidToken() {
        int storeId = 1;
        int userId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<OrderDTO>>> response = storeController.getAllStoreOrders(storeId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).getAllStoreOrders(anyInt(), anyInt());
    }

    @Test
    void testAddProductToStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 100.0;
        int quantity = 10;
        String category = "ELECTRONICS";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addProductToStore(storeId, requesterId, productId, basePrice, quantity, category))
                .thenReturn(new Response<>(null, "Product added successfully", true));

        ResponseEntity<Response<Void>> response = storeController.addProductToStore(storeId, requesterId, productId, basePrice, quantity, category, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addProductToStore(storeId, requesterId, productId, basePrice, quantity, category);
    }

    @Test
    void testRemoveProductFromStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeProductFromStore(storeId, requesterId, productId))
                .thenReturn(new Response<>(null, "Product removed successfully", true));

        ResponseEntity<Response<Void>> response = storeController.removeProductFromStore(storeId, requesterId, productId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).removeProductFromStore(storeId, requesterId, productId);
    }

    @Test
    void testCloseStoreByFounder_Success() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.closeStoreByFounder(storeId, userId))
                .thenReturn(new Response<>("Store closed successfully", "Store closed successfully", true));

        ResponseEntity<Response<String>> response = storeController.closeStoreByFounder(storeId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).closeStoreByFounder(storeId, userId);
    }

    @Test
    void testGetPendingOwners_Success() {
        int storeId = 1;
        int requesterId = 1;
        String token = "valid-token";
        List<Integer> pendingOwners = List.of(2, 3);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getPendingOwners(storeId, requesterId))
                .thenReturn(new Response<>(pendingOwners, "Pending owners retrieved successfully", true));

        ResponseEntity<Response<List<Integer>>> response = storeController.getPendingOwners(storeId, requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(pendingOwners, response.getBody().getData());
        verify(systemService, times(1)).getPendingOwners(storeId, requesterId);
    }

    @Test
    void testAcceptAssignment_Success() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.acceptAssignment(storeId, userId))
                .thenReturn(new Response<>("Assignment accepted successfully", "Assignment accepted successfully", true));

        ResponseEntity<Response<String>> response = storeController.acceptAssignment(storeId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).acceptAssignment(storeId, userId);
    }

    @Test
    void testDeclineAssignment_Success() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.declineAssignment(storeId, userId))
                .thenReturn(new Response<>("Assignment declined successfully", "Assignment declined successfully", true));

        ResponseEntity<Response<String>> response = storeController.declineAssignment(storeId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).declineAssignment(storeId, userId);
    }

    @Test
    void testUpdateProductInStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 150.0;
        int quantity = 5;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProductInStore(storeId, requesterId, productId, basePrice, quantity))
                .thenReturn(new Response<>(null, "Product updated successfully", true));

        ResponseEntity<Response<Void>> response = storeController.updateProductInStore(storeId, requesterId, productId, basePrice, quantity, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).updateProductInStore(storeId, requesterId, productId, basePrice, quantity);
    }

    @Test
    void testAddAuctionProductToStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 200.0;
        int daysToEnd = 7;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd))
                .thenReturn(new Response<>(null, "Auction product added successfully", true));

        ResponseEntity<Response<Void>> response = storeController.addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd);
    }

    @Test
    void testAddBidOnAuctionProductInStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double bid = 250.0;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addBidOnAuctionProductInStore(storeId, requesterId, productId, bid))
                .thenReturn(new Response<>(null, "Bid added successfully", true));

        ResponseEntity<Response<Void>> response = storeController.addBidOnAuctionProductInStore(storeId, requesterId, productId, bid, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addBidOnAuctionProductInStore(storeId, requesterId, productId, bid);
    }

    @Test
    void testRatingStore_Success() {
        int storeId = 1;
        int userId = 1;
        double rating = 4.5;
        String comment = "Great store!";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.ratingStore(storeId, userId, rating, comment))
                .thenReturn(new Response<>(null, "Store rated successfully", true));

        ResponseEntity<Response<Void>> response = storeController.ratingStore(storeId, userId, rating, comment, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).ratingStore(storeId, userId, rating, comment);
    }

    @Test
    void testAddStoreOwner_Success() {
        int storeId = 1;
        int requesterId = 1;
        int ownerId = 2;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStoreOwner(storeId, requesterId, ownerId))
                .thenReturn(new Response<>(null, "Store owner added successfully", true));

        ResponseEntity<Response<Void>> response = storeController.addStoreOwner(storeId, requesterId, ownerId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addStoreOwner(storeId, requesterId, ownerId);
    }

    @Test
    void testRemoveStoreOwner_Success() {
        int storeId = 1;
        int requesterId = 1;
        int ownerId = 2;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeStoreOwner(storeId, requesterId, ownerId))
                .thenReturn(new Response<>(null, "Store owner removed successfully", true));

        ResponseEntity<Response<Void>> response = storeController.removeStoreOwner(storeId, requesterId, ownerId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).removeStoreOwner(storeId, requesterId, ownerId);
    }

    @Test
    void testSendMessageToUser_Success() {
        int managerId = 1;
        int storeId = 1;
        int userId = 2;
        String message = "Hello!";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendMessageToUser(managerId, storeId, userId, message))
                .thenReturn(new Response<>(null, "Message sent successfully", true));

        ResponseEntity<Response<Void>> response = storeController.sendMessageToUser(managerId, storeId, userId, message, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).sendMessageToUser(managerId, storeId, userId, message);
    }



    @Test
    void testAddStore_InternalServerError() {
        int userId = 1;
        String storeName = "Test Store";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStore(userId, storeName))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR));

        ResponseEntity<Response<Void>> response = storeController.addStore(userId, storeName, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).addStore(userId, storeName);
    }

    @Test
    void testViewStore_BadRequest() {
        int storeId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.userAccessStore(token, storeId))
                .thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST));

        ResponseEntity<Response<StoreDTO>> response = storeController.viewStore(storeId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).userAccessStore(token, storeId);
    }

    @Test
    void testRemoveProductFromStore_InvalidToken() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.removeProductFromStore(storeId, requesterId, productId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).removeProductFromStore(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testAddAuctionProductToStore_InternalServerError() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 200.0;
        int daysToEnd = 7;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR));

        ResponseEntity<Response<Void>> response = storeController.addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).addAuctionProductToStore(storeId, requesterId, productId, basePrice, daysToEnd);
    }

    @Test
    void testGetPendingOwners_BadRequest() {
        int storeId = 1;
        int requesterId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getPendingOwners(storeId, requesterId))
                .thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST));

        ResponseEntity<Response<List<Integer>>> response = storeController.getPendingOwners(storeId, requesterId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getPendingOwners(storeId, requesterId);
    }
}