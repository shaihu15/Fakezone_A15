package com.fakezone.fakezone.ui.view;

import java.util.*;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.Enums.RoleName;
import ApplicationLayer.Response;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout implements BeforeEnterObserver {
   private final String backendUrl = "http://localhost:8080";
    private final RestTemplate restTemplate;

    // Define sets of roles for ownership and management
    private static final Set<String> OWNERSHIP_ROLES = Set.of(
        RoleName.STORE_FOUNDER.name(),
        RoleName.STORE_OWNER.name()
    );
    private static final Set<String> MANAGEMENT_ROLES = Set.of(
        RoleName.STORE_MANAGER.name()
    );

    public UserView() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        UserDTO userDTO = session != null ? (UserDTO) session.getAttribute("userDTO") : null;

        if (token == null || userDTO == null) {
            event.rerouteTo("login");
            Notification.show("Please log in to view this page.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (isGuestToken(session)) {
            event.rerouteTo("login");
            Notification.show("Please log in as a registered user to view this page.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Map<Integer, String> userRoles = getUserRoles(token);
        List<StoreDTO> ownedStores = new ArrayList<>();
        List<StoreDTO> managedStores = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : userRoles.entrySet()) {
            int storeId = entry.getKey();
            String roleType = entry.getValue();

            StoreDTO store = getStoreDTO(storeId, token);
            if (store != null) {
                String effectiveRole = (roleType != null) ? roleType : RoleName.UNASSIGNED.name();
                if (OWNERSHIP_ROLES.contains(effectiveRole)) {
                    ownedStores.add(store);
                } else if (MANAGEMENT_ROLES.contains(effectiveRole)) {
                    managedStores.add(store);
                } else {
                    Notification.show("Unsupported role type '" + effectiveRole + "' for store ID: " + storeId + ". Treated as UNASSIGNED.", 3000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Failed to load store with ID: " + storeId, 3000, Notification.Position.MIDDLE);
            }
        }

        // Create card for "My Stores"
        VerticalLayout ownedStoresCard = new VerticalLayout();
        ownedStoresCard.setPadding(true);
        ownedStoresCard.setSpacing(true);
        ownedStoresCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        ownedStoresCard.add(new H2("My Stores"));

        // Add "Open a Store" button as a general feature
        Button openStoreButton = new Button("Open a Store", e -> openStoreDialog(userDTO.getUserId(), token));
        ownedStoresCard.add(openStoreButton);

        if (!ownedStores.isEmpty()) {
            ownedStores.forEach(store -> ownedStoresCard.add(createStoreCard(store)));
        }

        // Create card for "Stores I Manage"
        VerticalLayout managedStoresCard = new VerticalLayout();
        managedStoresCard.setPadding(true);
        managedStoresCard.setSpacing(true);
        managedStoresCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        managedStoresCard.add(new H2("Stores I Manage"));

        if (!managedStores.isEmpty()) {
            managedStores.forEach(store -> managedStoresCard.add(createStoreCard(store)));
        }

        // Add both cards to the main layout
        add(ownedStoresCard, managedStoresCard);
    }

    private void openStoreDialog(int userId, String token) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Open a New Store");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField storeNameField = new TextField("Store Name");
        storeNameField.setPlaceholder("Enter store name");
        Button submitButton = new Button("Submit", e -> {
            String storeName = storeNameField.getValue().trim();
            if (!storeName.isEmpty()) {
                addStore(userId, storeName, token, dialog);
            } else {
                Notification.show("Please enter a store name.", 3000, Notification.Position.MIDDLE);
            }
        });
        dialogLayout.add(storeNameField, submitButton);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void addStore(int userId, String storeName, String token, Dialog dialog) {
        String url = backendUrl + "/api/store/addStore/" + userId + "/" + storeName;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Integer>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Store created successfully!", 3000, Notification.Position.MIDDLE);
                dialog.close();
                // Refresh the page to reflect the new store
                getUI().ifPresent(ui -> ui.getPage().reload());
            } else {
                Notification.show(response.getBody().getMessage(), 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Error creating store: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private boolean isGuestToken(HttpSession session) {
        String token = (String) session.getAttribute("token");
        RestTemplate restTemplate = new RestTemplate();
        String url = backendUrl + "/api/user/isGuestToken";
        try {
            ResponseEntity<Response> apiResponse = restTemplate.postForEntity(url, token, Response.class);
            Response<Boolean> response = (Response<Boolean>) apiResponse.getBody();
            return response.isSuccess() && response.getData();
        } catch (Exception e) {
            Notification.show("Error checking token: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            return true;
        }
    }

    private Map<Integer, String> getUserRoles(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = backendUrl + "/api/user/userRoles";

        try {
            ResponseEntity<Response<Map<Integer, LinkedHashMap<String, Object>>>> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            Map<Integer, String> roleMap = new HashMap<>();
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                for (Map.Entry<Integer, LinkedHashMap<String, Object>> entry : response.getBody().getData().entrySet()) {
                    Integer storeId = entry.getKey();
                    LinkedHashMap<String, Object> value = entry.getValue();
                    String type = (String) value.get("type"); // Safely get "type" (may be null)
                    if (type != null) {
                        roleMap.put(storeId, type);
                    } else {
                        roleMap.put(storeId, RoleName.UNASSIGNED.name()); // Default to UNASSIGNED
                    }
                }
            }
            return roleMap;
        } catch (Exception e) {
            Notification.show("Failed to fetch user roles: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            return Collections.emptyMap();
        }
    }

    private StoreDTO getStoreDTO(int storeId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format(backendUrl + "/api/store/viewStore/%d", storeId);

        try {
            ResponseEntity<Response<StoreDTO>> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Component createStoreCard(StoreDTO store) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        Map<String, String> parameters = Collections.singletonMap("storeId", String.valueOf(store.getStoreId()));
        RouterLink link = new RouterLink(store.getName(), StoreView.class);
        link.setQueryParameters(QueryParameters.simple(parameters));
        link.getStyle().set("font-size", "1.5em");

        layout.add(link);
        layout.add(new Span("ID: " + store.getStoreId()));
        layout.add(new Span("Open: " + (store.isOpen() ? "Yes" : "No")));
        layout.add(new Span("Average Rating: " + store.getRatings().values().stream().mapToDouble(d -> d).average().orElse(0.0)));

        return layout;
    }
}