package InfrastructureLayer.Adapters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalPaymentSystem {
    private static final String API_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(ExternalPaymentSystem.class);
    public ExternalPaymentSystem() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public int processPayment(String cardNumber, String cardHolder, String expDate, String cvv, double amount,int userId) {
        try {
            // Parse expDate to separate month and year
            String month = "";
            String year = "";
            if (expDate != null && expDate.contains("/")) {
                String[] parts = expDate.split("/");
                if (parts.length == 2) {
                    month = parts[0];
                    year = parts[1];
                    // If year is 2-digit, convert to 4-digit
                    if (year.length() == 2) {
                        year = "20" + year;
                    }
                }
            }   else {
                    logger.error("Invalid expiration date format: {}", expDate);
                    return -1;
            }
            // Create form-data payload for payment request
            String formData = String.format(
                "action_type=pay&amount=%.0f&currency=USD&card_number=%s&month=%s&year=%s&holder=%s&cvv=%s&id=%d",
                amount, 
                urlEncode(cardNumber), 
                urlEncode(month), 
                urlEncode(year), 
                urlEncode(cardHolder), 
                urlEncode(cvv),
                urlEncode(String.valueOf(userId))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // if body is a number between  [10000, 100000] return true, else return false
            int body = Integer.parseInt(response.body());
            if (body >= 10000 && body <= 100000) {
                return body; // Return the transaction ID
            }
            logger.error("Payment failed with response: {}", response.body());
            return -1;
            
        } catch (IOException | InterruptedException | NumberFormatException e) {
            logger.error("Error processing payment: " + e.getMessage());
            return -1;
        }
    }

    public int processRefund(int transactionId) {
        try {
            // Create form-data payload for cancel payment request
            String formData =  String.format("action_type=cancel_pay&transaction_id=%d", transactionId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // if body is a number between  [10000, 100000] return true, else return false
            int body = Integer.parseInt(response.body());
            if (body == 1) {
                return 1;
            }
            logger.error("Process refund failed. External system returned: {}", body);
            return -1;
            
        } catch (IOException | InterruptedException | NumberFormatException e) {
            System.err.println("Error processing refund: " + e.getMessage());
            return -1;
        }
    }

    // Helper method to URL encode form data
    private String urlEncode(String value) {
        if (value == null) return "";
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }
}