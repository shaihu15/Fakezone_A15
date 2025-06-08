package InfrastructureLayer.Adapters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalDeliverySystem {
    private static final String API_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(ExternalDeliverySystem.class);
    public ExternalDeliverySystem() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // For testing: allow injection of a mock HttpClient
    protected HttpClient getHttpClient() {
        return this.httpClient;
    }

    public int sendPackage(String fullAddress, String recipient, String packageDetails) {
        try {
            if (fullAddress == null || recipient == null || packageDetails == null) {
                logger.error("Null argument(s) in sendPackage: address={}, recipient={}, packageDetails={}", fullAddress, recipient, packageDetails);
                return -1;
            }
            String[] addressParts = parseAddress(fullAddress);
            if (addressParts.length != 4) {
                logger.error("Invalid address format: {}", fullAddress);
                return -1; // Return -1 on invalid format
            }
            String address = addressParts[0]; // This is the street address
            String city = addressParts[1];
            String country = addressParts[2];
            String zip = addressParts[3];
            // Create form-data payload for supply request
            String formData = String.format(
                "action_type=supply&name=%s&address=%s&city=%s&country=%s&zip=%s",
                urlEncode(recipient), urlEncode(address),
                urlEncode(city),    
                urlEncode(country),
                urlEncode(zip)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            // if body is a number between  [10000, 100000] return true, else return false
            int body = Integer.parseInt(response.body());
            if (body >= 10000 && body <= 100000) {
                return body; // Return the transaction ID
            }
            return -1; // Return -1 on failure
        } catch (IOException | InterruptedException | NumberFormatException e) {
            logger.error("Error sending package: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            logger.error("Address parsing error: " + e.getMessage());
            return -1; // Indicate failure due to parsing
        }
    }

    public int cancelPackage(int transaction_Id) {
        try {
            // Create form-data payload for cancel_supply request
            String formData = String.format(
                "action_type=cancel_supply&transaction_id=%d",
                transaction_Id
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            // if body is a number between  [10000, 100000] return true, else return false
            int body = Integer.parseInt(response.body());
             if (body == 1) {
                return 1;
            }
            return -1;
        } catch (IOException | InterruptedException | NumberFormatException e) {
            logger.error("Error canceling package: " + e.getMessage());
            return -1;
        }
    }

    public String handshake() {
        try {
            String formData = "action_type=handshake";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            logger.error("Error during handshake: " + e.getMessage());
            return null;
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
    // This method is used to parse the full address into its components to avoid changeing parameters in the deliver method
    private String[] parseAddress(String fullAddress) {
        if (fullAddress == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        String[] parts = fullAddress.split("\\*");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid address format. Expected: street*city*country*zip");
        }
        return parts;
    }
}

