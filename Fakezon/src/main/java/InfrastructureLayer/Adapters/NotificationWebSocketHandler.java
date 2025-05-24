package InfrastructureLayer.Adapters;

import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Services.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Component

public class NotificationWebSocketHandler extends TextWebSocketHandler implements INotificationWebSocketHandler {
    // Need to set up and create events for the client to subscribe to those events can be created from string or enum
    // Map of event types to connected WebSocket sessions
    private static final Map<String, Set<WebSocketSession>> eventSubscriptions = Collections.synchronizedMap(new HashMap<>());
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String event = message.getPayload();  // Expecting event type as message
        eventSubscriptions.computeIfAbsent(event,
                k -> Collections.synchronizedSet(new HashSet<>())).add(session);
        logger.info("Client subscribed to event: " + event);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        eventSubscriptions.forEach((event, sessions) -> sessions.remove(session));
        logger.info("WebSocket connection closed: " + session.getId());
    }

    public void broadcast(String event, String message) {
        Set<WebSocketSession> sessions = eventSubscriptions.get(event);
        if (sessions != null) {
            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        logger.error("Error sending message to session " + session.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
