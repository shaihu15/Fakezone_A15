package UnitTesting;

import ApplicationLayer.Response;
import com.fakezone.fakezone.controller.HomeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    private HomeController homeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        homeController = new HomeController();
    }

    @Test
    void testHome_Success() {
        ResponseEntity<Response<String>> response = homeController.home();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("home", response.getBody().getData());
        assertEquals("Welcome to the home page!", response.getBody().getMessage());
    }

    @Test
    void testHome_InternalError() {
        HomeController faultyController = spy(new HomeController());
        doThrow(new RuntimeException("Simulated error")).when(faultyController).home();

        ResponseEntity<Response<String>> response = null;
        try {
            response = faultyController.home();
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals("Simulated error", e.getMessage());
        }

        assertNull(response); // Ensure no valid response is returned
    }
}