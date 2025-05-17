package UnitTesting;

import ApplicationLayer.RequestDataTypes.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterUserRequestTest {

    @Test
    void testConstructorAndGetters() {
        String email = "test@example.com";
        String password = "securePass";
        String dateOfBirth = "2000-01-01";
        String country = "Israel";

        RegisterUserRequest request = new RegisterUserRequest(email, password, dateOfBirth, country);

        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(dateOfBirth, request.getDateOfBirth());
        assertEquals(country, request.getCountry());
    }

    @Test
    void testSetters() {
        RegisterUserRequest request = new RegisterUserRequest("", "", "", "");

        request.setEmail("new@example.com");
        assertEquals("new@example.com", request.getEmail());

        request.setPassword("newPass");
        assertEquals("newPass", request.getPassword());

        request.setDateOfBirth("1999-12-31");
        assertEquals("1999-12-31", request.getDateOfBirth());

        request.setCountry("USA");
        assertEquals("USA", request.getCountry());
    }
}
