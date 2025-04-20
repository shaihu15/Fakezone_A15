package DomainLayer.Model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Message {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int messageId;
    private final int senderId;
    private final String content;
    private final LocalDateTime timestamp;
    private boolean isRead; // Optional: track if the message has been read by store staff

    public Message(int senderId, String content) {
        this.messageId = idCounter.incrementAndGet();
        this.senderId = senderId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false; // Default to unread
    }

    // Getters
    public int getMessageId() {
        return messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    // Setter for read status (optional)
    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return "Message{" +
               "messageId=" + messageId +
               ", senderId=" + senderId +
               ", content=\"" + content + "\"" + // Escaped double quotes for content
               ", timestamp=" + timestamp +
               ", isRead=" + isRead +
               '}';
    }
}
