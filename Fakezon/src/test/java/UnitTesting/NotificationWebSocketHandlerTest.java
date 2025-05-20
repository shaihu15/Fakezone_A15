package UnitTesting;

import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

import static org.mockito.Mockito.*;

public class NotificationWebSocketHandlerTest {

    private NotificationWebSocketHandler handler;

    @Mock
    private WebSocketSession mockSession1;

    @Mock
    private WebSocketSession mockSession2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new NotificationWebSocketHandler();

        when(mockSession1.getId()).thenReturn("session1");
        when(mockSession2.getId()).thenReturn("session2");
    }

    @Test
    void afterConnectionEstablished_ShouldLogAndDoNothingElse() throws Exception {
        handler.afterConnectionEstablished(mockSession1);
        verify(mockSession1).getId();
    }

    @Test
    void handleTextMessage_ShouldSubscribeSessionToEvent() throws Exception {
        String event = "event-type";
        TextMessage message = new TextMessage(event);

        handler.handleTextMessage(mockSession1, message);

        // Send another session to same event
        handler.handleTextMessage(mockSession2, message);

        // Broadcast to verify subscriptions actually worked
        handler.broadcast(event, "Hello Subscribers");

        verify(mockSession1).sendMessage(new TextMessage("Hello Subscribers"));
        verify(mockSession2).sendMessage(new TextMessage("Hello Subscribers"));
    }

    @Test
    void afterConnectionClosed_ShouldUnsubscribeSessionFromAllEvents() throws Exception {
        String event1 = "event1";
        String event2 = "event2";

        // Subscribing session to multiple events
        handler.handleTextMessage(mockSession1, new TextMessage(event1));
        handler.handleTextMessage(mockSession1, new TextMessage(event2));

        // Disconnect the session
        handler.afterConnectionClosed(mockSession1, CloseStatus.NORMAL);

        // Try to broadcast to verify it's unsubscribed
        handler.broadcast(event1, "Message1");
        handler.broadcast(event2, "Message2");

        verify(mockSession1, never()).sendMessage(any());
    }

    @Test
    void broadcast_WhenSessionThrowsException_ShouldLogAndContinue() throws Exception {
        String event = "eventX";
        handler.handleTextMessage(mockSession1, new TextMessage(event));
}
}