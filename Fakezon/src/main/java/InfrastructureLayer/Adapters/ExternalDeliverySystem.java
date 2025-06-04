package InfrastructureLayer.Adapters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ExternalDeliverySystem {
    private static final String API_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private final HttpClient httpClient;

    public ExternalDeliverySystem() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean sendPackage(String address, String recipient, String packageDetails) {
        try {
            // Create form-data payload for supply request
            String formData = String.format(
                "action_type=supply&name=%s&address=%s&city=Beer%%20Sheva&country=Israel&zip=8458527",
                urlEncode(recipient), urlEncode(address)
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
            System.err.println("Error sending package: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelPackage(int deliveryId) {
        try {
            // Create form-data payload for cancel_supply request
            String formData = String.format(
                "action_type=cancel_supply&transaction_id=%d",
                deliveryId
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
            System.err.println("Error canceling package: " + e.getMessage());
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

