package UnitTesting;

import InfrastructureLayer.Adapters.ExternalDeliverySystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalDeliverySystemTest {

    private ExternalDeliverySystem deliverySystem;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    @BeforeEach
    void setUp() throws Exception {
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockResponse = Mockito.mock(HttpResponse.class);
        deliverySystem = new ExternalDeliverySystem() {
            @Override
            protected HttpClient getHttpClient() {
                return mockHttpClient;
            }
        };
    }

    @Test
    void sendPackage_ShouldReturnTransactionId_ForValidInput() throws Exception {
        // Arrange
        String address = "Main St*City*Country*12345";
        String recipient = "John Doe";
        String packageDetails = "Electronics";
        Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);
        Mockito.when(mockResponse.body()).thenReturn("12345");

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result > 0);
    }

    @Test
    void sendPackage_ShouldReturnMinusOne_ForInvalidInput() throws Exception {
        // Arrange
        String address = null;
        String recipient = "John Doe";
        String packageDetails = "Electronics";
        // No need to mock HTTP client, should fail before request

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void cancelPackage_ShouldReturnOne_ForValidTransactionId() throws Exception {
        // Arrange
        int deliveryId = 12345;
        Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);
        Mockito.when(mockResponse.body()).thenReturn("1");

        // Act
        int result = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void cancelPackage_ShouldReturnMinusOne_ForInvalidTransactionId() throws Exception {
        // Arrange
        int deliveryId = -1;
        Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);
        Mockito.when(mockResponse.body()).thenReturn("-1");

        // Act
        int result = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void handshake_ShouldReturnOK() throws Exception {
        Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);
        Mockito.when(mockResponse.body()).thenReturn("OK");

        String result = deliverySystem.handshake();
        assertEquals("OK", result);
    }

    @Test
    void sendPackage_ShouldReturnCorrectTransactionId() throws Exception {
        // Arrange
        String address = "Main St*City*Country*12345";
        String recipient = "John Doe";
        String packageDetails = "Electronics";
        Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);
        Mockito.when(mockResponse.body()).thenReturn("54321");

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertEquals(54321, result, "Should return the exact transaction ID from the response body");
    }
}
