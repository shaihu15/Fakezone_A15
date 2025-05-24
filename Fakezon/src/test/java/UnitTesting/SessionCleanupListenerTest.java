package UnitTesting;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fakezone.fakezone.Listener.SessionCleanupListener;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionCleanupListenerTest {

    @InjectMocks
    SessionCleanupListener listener;

    @Mock
    RestTemplate restTemplate;

    @Mock
    HttpSessionEvent event;

    @Mock
    HttpSession session;

    final String apiUrl = "http://localhost:8080/api/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener.setApiUrl(apiUrl);
        when(event.getSession()).thenReturn(session);
    }

    @Test
    void testSessionDestroyed_GuestToken() {
        String token = "guest-token";
        UserDTO user = new UserDTO();
        user.setUserId(1);

        when(session.getAttribute("token")).thenReturn(token);
        when(session.getAttribute("userDTO")).thenReturn(user);

        Response<Boolean> response = new Response<>(true, null, true, null, null);

        ResponseEntity<Response<Boolean>> guestCheckResponse = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(apiUrl + "user/isGuestToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Response<Boolean>>>any())).thenReturn(guestCheckResponse);

        when(restTemplate.exchange(
                eq(apiUrl + "user/removeUnsignedUser/" + user.getUserId()),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Response<Boolean>>>any()))
                .thenReturn(ResponseEntity.ok(new Response<>()));

        listener.setRestTemplate(restTemplate); // override default instance
        listener.sessionDestroyed(event);

        verify(session).removeAttribute("token");
        verify(session).removeAttribute("userDTO");
    }

    @Test
    void testSessionDestroyed_RegisteredUser() {
        String token = "valid-token";
        UserDTO user = new UserDTO();
        user.setUserId(42);

        when(session.getAttribute("token")).thenReturn(token);
        when(session.getAttribute("userDTO")).thenReturn(user);

        Response<Boolean> response = new Response<>(false, null, true, null, null);

        ResponseEntity<Response<Boolean>> guestCheckResponse = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(apiUrl + "user/isGuestToken"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Response<Boolean>>>any())).thenReturn(guestCheckResponse);

        when(restTemplate.exchange(
                eq(apiUrl + "user/logout"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Response<Void>>>any()))
                .thenReturn(ResponseEntity.ok(new Response<>()));

        listener.setRestTemplate(restTemplate);
        listener.sessionDestroyed(event);

        verify(session).removeAttribute("token");
        verify(session).removeAttribute("userDTO");
    }
}
