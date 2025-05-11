package UnitTesting;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Response;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Store;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import com.fakezone.fakezone.controller.StoreController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ApplicationLayer.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;
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
        when(systemService.addStore(userId, storeName)).thenReturn(new Response<>(null, "Store added successfully", true, null, null));

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

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

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

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
        String productName = "Test Product";
        String description = "Test Description";
        double basePrice = 100.0;
        int quantity = 10;
        String category = "ELECTRONICS";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category))
                .thenReturn(new Response<>(null, "Product added successfully", true, null, null));

        ResponseEntity<Response<StoreProductDTO>> response = storeController.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);
    }

    @Test
    void testRemoveProductFromStore_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeProductFromStore(storeId, requesterId, productId))
                .thenReturn(new Response<>(null, "Product removed successfully", true, null, null));

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
                .thenReturn(new Response<>("Store closed successfully", "Store closed successfully", true, null, null));

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
                .thenReturn(new Response<>(pendingOwners, "Pending owners retrieved successfully", true, null, null));

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
                .thenReturn(new Response<>("Assignment accepted successfully", "Assignment accepted successfully", true, null, null));

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
                .thenReturn(new Response<>("Assignment declined successfully", "Assignment declined successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Product updated successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Auction product added successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Bid added successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Store rated successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Store owner added successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Store owner removed successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Message sent successfully", true, null, null));

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
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

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
                .thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));

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
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

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
                .thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<Integer>>> response = storeController.getPendingOwners(storeId, requesterId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getPendingOwners(storeId, requesterId);
    }

    @Test
    void testViewStore_Success() {
        int storeId = 1;
        String token = "valid-token";
        Map<Integer, Double> ratings = new HashMap<>();
        StoreDTO storeDTO = new StoreDTO(storeId, "Test Store", 1, true, List.of(), ratings, 0.0);        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.userAccessStore(token, storeId))
                .thenReturn(new Response<>(storeDTO, "Store retrieved successfully", true, null, null));

        ResponseEntity<Response<StoreDTO>> response = storeController.viewStore(storeId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(storeDTO, response.getBody().getData());
        verify(systemService, times(1)).userAccessStore(token, storeId);
    }

    @Test
    void testViewStore_InvalidToken() {
        int storeId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<StoreDTO>> response = storeController.viewStore(storeId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).userAccessStore(anyString(), anyInt());
    }

    @Test
    void testUpdateProductInStore_InvalidToken() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 150.0;
        int quantity = 5;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.updateProductInStore(storeId, requesterId, productId, basePrice, quantity, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).updateProductInStore(anyInt(), anyInt(), anyInt(), anyDouble(), anyInt());
    }

    @Test
    void testRatingStore_BadRequest() {
        int storeId = 1;
        int userId = 1;
        double rating = 6.0; // Invalid rating (assuming max is 5.0)
        String comment = "Great store!";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.ratingStore(storeId, userId, rating, comment))
                .thenReturn(new Response<>(null, "Invalid rating value", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Void>> response = storeController.ratingStore(storeId, userId, rating, comment, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).ratingStore(storeId, userId, rating, comment);
    }

    @Test
    void testAddStoreOwner_BadRequest() {
        int storeId = 1;
        int requesterId = 1;
        int ownerId = -1; // Invalid owner ID
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStoreOwner(storeId, requesterId, ownerId))
                .thenReturn(new Response<>(null, "Invalid owner ID", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Void>> response = storeController.addStoreOwner(storeId, requesterId, ownerId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).addStoreOwner(storeId, requesterId, ownerId);
    }

    @Test
    void testRemoveStoreManager_InternalServerError() {
        int storeId = 1;
        int requesterId = 1;
        int managerId = 2;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeStoreManager(storeId, requesterId, managerId))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Void>> response = storeController.removeStoreManager(storeId, requesterId, managerId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).removeStoreManager(storeId, requesterId, managerId);
    }

    @Test
    void testSendMessageToUser_BadRequest() {
        int managerId = 1;
        int storeId = 1;
        int userId = 2;
        String message = ""; // Empty message
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendMessageToUser(managerId, storeId, userId, message))
                .thenReturn(new Response<>(null, "Message cannot be empty", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Void>> response = storeController.sendMessageToUser(managerId, storeId, userId, message, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).sendMessageToUser(managerId, storeId, userId, message);
    }

    @Test
    void testAddStore_BadRequest() {
        int userId = 1;
        String storeName = ""; // Invalid store name
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStore(userId, storeName))
                .thenReturn(new Response<>(null, "Store name cannot be empty", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).addStore(userId, storeName);
    }

    @Test
    void testRemoveStoreOwner_InvalidToken() {
        int storeId = 1;
        int requesterId = 1;
        int ownerId = 2;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.removeStoreOwner(storeId, requesterId, ownerId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).removeStoreOwner(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testGetAllStoreOrders_InternalServerError() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllStoreOrders(storeId, userId))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<List<OrderDTO>>> response = storeController.getAllStoreOrders(storeId, userId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getAllStoreOrders(storeId, userId);
    }

    @Test
    void testAddBidOnAuctionProductInStore_BadRequest() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double bid = -50.0; // Invalid bid
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addBidOnAuctionProductInStore(storeId, requesterId, productId, bid))
                .thenReturn(new Response<>(null, "Bid amount must be positive", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Void>> response = storeController.addBidOnAuctionProductInStore(storeId, requesterId, productId, bid, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).addBidOnAuctionProductInStore(storeId, requesterId, productId, bid);
    }

    @Test
    void testGetPendingManagers_Success() {
        int storeId = 1;
        int requesterId = 1;
        String token = "valid-token";
        List<Integer> pendingManagers = List.of(3, 4);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getPendingManagers(storeId, requesterId))
                .thenReturn(new Response<>(pendingManagers, "Pending managers retrieved successfully", true, null, null));

        ResponseEntity<Response<List<Integer>>> response = storeController.getPendingManagers(storeId, requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(pendingManagers, response.getBody().getData());
        verify(systemService, times(1)).getPendingManagers(storeId, requesterId);
    }

    @Test
    void testCloseStoreByFounder_InvalidToken() {
        int storeId = 1;
        int userId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<String>> response = storeController.closeStoreByFounder(storeId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).closeStoreByFounder(anyInt(), anyInt());
    }

    @Test
    void testSendResponseForAuctionByOwner_Success() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        boolean accept = true;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept))
                .thenReturn(new Response<>("Auction response sent successfully", "Success", true, null, null));

        ResponseEntity<Response<String>> response = storeController.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Auction response sent successfully", response.getBody().getData());
        verify(systemService, times(1)).sendResponseForAuctionByOwner(storeId, requesterId, productId, accept);
    }




    @Test
    void testAcceptAssignment_InvalidToken() {
        int storeId = 1;
        int userId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<String>> response = storeController.acceptAssignment(storeId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).acceptAssignment(anyInt(), anyInt());
    }



    @Test
    void testAddProductToStore_BadRequest() {
        int storeId = 1;
        int requesterId = 1;
        String productName = ""; // Invalid product name
        String description = "Test Description";
        double basePrice = 100.0;
        int quantity = 10;
        String category = "ELECTRONICS";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category))
                .thenReturn(new Response<>(null, "Invalid product name", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<StoreProductDTO>> response = storeController.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);
    }

    @Test
    void testRemoveProductFromStore_InternalServerError() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeProductFromStore(storeId, requesterId, productId))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Void>> response = storeController.removeProductFromStore(storeId, requesterId, productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).removeProductFromStore(storeId, requesterId, productId);
    }

    @Test
    void testRatingStore_InvalidToken() {
        int storeId = 1;
        int userId = 1;
        double rating = 4.5;
        String comment = "Great store!";
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.ratingStore(storeId, userId, rating, comment, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).ratingStore(anyInt(), anyInt(), anyDouble(), anyString());
    }

    @Test
    void testGetStoreRoles_InternalServerError() {
        int storeId = 1;
        int requesterId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getStoreRoles(storeId, requesterId))
                .thenReturn(new Response<>(null, "Internal server error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<StoreRolesDTO>> response = storeController.getStoreRoles(storeId, requesterId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getStoreRoles(storeId, requesterId);
    }

    @Test
    void testDeclineAssignment_BadRequest() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.declineAssignment(storeId, userId))
                .thenReturn(new Response<>(null, "Invalid assignment", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<String>> response = storeController.declineAssignment(storeId, userId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).declineAssignment(storeId, userId);
    }

    @Test
    void testAddStore_ExceptionHandling() {
        int userId = 1;
        String storeName = "Test Store";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStore(userId, storeName)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).addStore(userId, storeName);
    }

    @Test
    void testAddProductToStore_ExceptionHandling() {
        int storeId = 1;
        int requesterId = 1;
        String productName = "Test Product";
        String description = "Test Description";
        double basePrice = 100.0;
        int quantity = 10;
        String category = "ELECTRONICS";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<StoreProductDTO>> response = storeController.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);
    }

    @Test
    void testViewStore_ExceptionHandling() {
        int storeId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.userAccessStore(token, storeId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<StoreDTO>> response = storeController.viewStore(storeId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).userAccessStore(token, storeId);
    }

    @Test
    void testUpdateProductInStore_ExceptionHandling() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        double basePrice = 100.0;
        int quantity = 5;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProductInStore(storeId, requesterId, productId, basePrice, quantity))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Void>> response = storeController.updateProductInStore(storeId, requesterId, productId, basePrice, quantity, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProductInStore(storeId, requesterId, productId, basePrice, quantity);
    }

    @Test
    void testRemoveProductFromStore_ExceptionHandling() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeProductFromStore(storeId, requesterId, productId))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Void>> response = storeController.removeProductFromStore(storeId, requesterId, productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).removeProductFromStore(storeId, requesterId, productId);
    }

    @Test
    void testAddStore_Unauthorized() {
        int userId = 1;
        String storeName = "Test Store";
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Integer>> response = storeController.addStore(userId, storeName, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).addStore(anyInt(), anyString());
    }

    @Test
    void testAddProductToStore_InvalidCategory() {
        int storeId = 1;
        int requesterId = 1;
        String productName = "Test Product";
        String description = "Test Description";
        double basePrice = 100.0;
        int quantity = 10;
        String category = "INVALID_CATEGORY";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category))
                .thenReturn(new Response<>(null, "Invalid category", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<StoreProductDTO>> response = storeController.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);
    }

    @Test
    void testCloseStoreByFounder_ExceptionHandling() {
        int storeId = 1;
        int userId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.closeStoreByFounder(storeId, userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<String>> response = storeController.closeStoreByFounder(storeId, userId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).closeStoreByFounder(storeId, userId);
    }

    @Test
    void testAddStoreManagerPermissions_InvalidToken() {
        int storeId = 1;
        int managerId = 1;
        List<String> permissions = List.of("VIEW_PURCHASES");
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.addStoreManagerPermissions(storeId, managerId, permissions, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).addStoreManagerPermissions(anyInt(), anyString(), anyInt(), anyList());
    }
    @Test
    void testRatingStoreProduct_Success() {
        int storeId = 1;
        int productId = 1;
        int userId = 1;
        double rating = 4.5;
        String comment = "Great product!";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.ratingStoreProduct(storeId, productId, userId, rating, comment))
                .thenReturn(new Response<>(null, "Product rated successfully", true, null, null));

        ResponseEntity<Response<Void>> response = storeController.ratingStoreProduct(storeId, productId, userId, rating, comment, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).ratingStoreProduct(storeId, productId, userId, rating, comment);
    }

    @Test
    void testRatingStoreProduct_InvalidToken() {
        int storeId = 1;
        int productId = 1;
        int userId = 1;
        double rating = 4.5;
        String comment = "Great product!";
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.ratingStoreProduct(storeId, productId, userId, rating, comment, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, never()).ratingStoreProduct(anyInt(), anyInt(), anyInt(), anyDouble(), anyString());
    }



    @Test
    void testRemoveStoreManagerPermissions_InvalidToken() {
        int storeId = 1;
        int managerId = 1;
        String token = "invalid-token";
        Request<List<String>> request = new Request<>(token, List.of("VIEW_PURCHASES"));

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = storeController.removeStoreManagerPermissions(storeId, managerId, request, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, never()).removeStoreManagerPermissions(anyInt(), anyString(), anyInt(), anyList());
    }

    @Test
    void testAddStoreManagerPermissions_Success() {
        int storeId = 1;
        int managerId = 1;
        List<String> permissions = List.of("VIEW_PURCHASES");
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addStoreManagerPermissions(storeId, token, managerId, permissions.stream()
                .map(StoreManagerPermission::valueOf)
                .toList()))
                .thenReturn(new Response<>(null, "Permissions added successfully", true, null, null));

        ResponseEntity<Response<Void>> response = storeController.addStoreManagerPermissions(storeId, managerId, permissions, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(systemService, times(1)).addStoreManagerPermissions(storeId, token, managerId, permissions.stream()
                .map(StoreManagerPermission::valueOf)
                .toList());
    }


    @Test
    void testSendResponseForAuctionByOwner_InvalidToken() {
        int storeId = 1;
        int requesterId = 1;
        int productId = 1;
        boolean accept = true;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<String>> response = storeController.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, never()).sendResponseForAuctionByOwner(anyInt(), anyInt(), anyInt(), anyBoolean());
    }


    @Test
    void testGetPendingManagers_InvalidToken() {
        int storeId = 1;
        int requesterId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<Integer>>> response = storeController.getPendingManagers(storeId, requesterId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, never()).getPendingManagers(anyInt(), anyInt());
    }



}