package InfrastructureLayer.Adapters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection; // <-- New Import

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalPaymentSystem {
    private static final String API_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(ExternalPaymentSystem.class);

    public ExternalPaymentSystem() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // --- ADDED THIS LINE FOR MORE AGGRESSIVE HOSTNAME BYPASS ---
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            // ------------------------------------------------------------

            this.httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize HttpClient with SSL context to bypass validation.", e);
            throw new RuntimeException("Failed to create secure HttpClient for external API calls.", e);
        }
    }

    protected HttpClient getHttpClient() {
        return this.httpClient;
    }

    public int processPayment(String cardNumber, String cardHolder, String expDate, String cvv, double amount, int id) {
        try {
            if (cardNumber == null || cardHolder == null || expDate == null || cvv == null) {
                logger.error("Null argument(s) in processPayment: cardNumber={}, cardHolder={}, expDate={}, cvv={}", cardNumber, cardHolder, expDate, cvv);
                return -1;
            }
            String month = "";
            String year = "";
            if (expDate != null && expDate.contains("/")) {
                String[] parts = expDate.split("/");
                if (parts.length == 2) {
                    month = parts[0];
                    year = parts[1];
                    if (year.length() == 2) {
                        year = "20" + year;
                    }
                } else {
                    logger.error("Invalid expiration date format: {}", expDate);
                    return -1;
                }
            } else {
                logger.error("Invalid expiration date format: {}", expDate);
                return -1;
            }
            String formData = String.format(
                 "action_type=pay&amount=%.0f&currency=USD&card_number=%s&month=%s&year=%s&holder=%s&cvv=%s&id=%s",
                amount, 
                urlEncode(cardNumber), 
                urlEncode(month), 
                urlEncode(year), 
                urlEncode(cardHolder), 
                urlEncode(cvv),
                urlEncode(String.valueOf(id))
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            int body = Integer.parseInt(response.body());
            if (body >= 10000 && body <= 100000) {
                return body;
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
            String formData = String.format("action_type=cancel_pay&transaction_id=%d", transactionId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
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
            String body = response.body();
            if (body != null && body.trim().equalsIgnoreCase("ok")) {
                return "OK";
            } else {
                logger.error("Unexpected handshake response: {}", body);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error during handshake: " + e.getMessage());
            return null;
        }
    }

    private String urlEncode(String value) {
        if (value == null) return "";
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }
}