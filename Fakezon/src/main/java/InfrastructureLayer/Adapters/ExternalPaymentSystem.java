package InfrastructureLayer.Adapters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ExternalPaymentSystem {
    private static final String API_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private final HttpClient httpClient;

    public ExternalPaymentSystem() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean processPayment(String cardNumber, String cardHolder, String expDate, String cvv, double amount) {
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
            }

            // Create form-data payload for payment request
            String formData = String.format(
                "action_type=pay&amount=%.0f&currency=USD&card_number=%s&month=%s&year=%s&holder=%s&cvv=%s&id=20444444",
                amount, 
                urlEncode(cardNumber), 
                urlEncode(month), 
                urlEncode(year), 
                urlEncode(cardHolder), 
                urlEncode(cvv)
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
                return true;
            }
            return false;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }

    public boolean processRefund(String cardNumber, double amount) {
        try {
            // Create form-data payload for cancel payment request
            String formData = "action_type=cancel_pay&transaction_id=20123";

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
                return true;
            }
            return false;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error processing refund: " + e.getMessage());
            return false;
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