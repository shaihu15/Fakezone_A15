package DomainLayer.Interfaces;

import InfrastructureLayer.Enums.NotificationEvent;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface INotificationWebSocketHandler {
    void afterConnectionEstablished(WebSocketSession session);
    void handleTextMessage(WebSocketSession session, TextMessage message);
    void afterConnectionClosed(WebSocketSession session, CloseStatus status);
    void broadcast(String event, String message);
}
