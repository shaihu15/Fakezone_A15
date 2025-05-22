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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import ApplicationLayer.Response;
import DomainLayer.Enums.RoleName;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout implements BeforeEnterObserver {
     private final String backendUrl = "http://localhost:8080";
    private final RestTemplate restTemplate;
    private VerticalLayout pendingAssignmentsCard;

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
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
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

        int userId = userDTO.getUserId();
        String tokenValue = token;

        Map<Integer, String> userRoles = getUserRoles(token);
        List<Component> ownedStores = new ArrayList<>();
        List<Component> managedStores = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : userRoles.entrySet()) {
            int storeId = entry.getKey();
            String roleType = entry.getValue();

            StoreDTO store = getStoreDTO(storeId, token);
            if (store != null) {
                String effectiveRole = (roleType != null) ? roleType : RoleName.UNASSIGNED.name();
                if (OWNERSHIP_ROLES.contains(effectiveRole)) {
                    ownedStores.add(createStoreCard(store, effectiveRole));
                } else if (MANAGEMENT_ROLES.contains(effectiveRole)) {
                    managedStores.add(createStoreCard(store, effectiveRole));
                } else {
                    Notification.show("Unsupported role type '" + effectiveRole + "' for store ID: " + storeId + ". Treated as UNASSIGNED.", 3000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Failed to load store with ID: " + storeId, 3000, Notification.Position.MIDDLE);
            }
        }

        Button openStoreButton = new Button("Open a Store", e -> openStoreDialog(userId, token));
        add(openStoreButton);

        VerticalLayout ownedStoresCard = new VerticalLayout();
        ownedStoresCard.setPadding(true);
        ownedStoresCard.setSpacing(true);
        ownedStoresCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        ownedStoresCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        ownedStoresCard.add(new H2("My Stores"));

        if (!ownedStores.isEmpty()) {
            ownedStores.forEach(ownedStoresCard::add);
        } else {
            ownedStoresCard.add(new Span("You do not own any stores."));
        }

        VerticalLayout managedStoresCard = new VerticalLayout();
        managedStoresCard.setPadding(true);
        managedStoresCard.setSpacing(true);
        managedStoresCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        managedStoresCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        managedStoresCard.add(new H2("Stores I Manage"));

        if (!managedStores.isEmpty()) {
            managedStores.forEach(managedStoresCard::add);
        } else {
            managedStoresCard.add(new Span("You do not manage any stores."));
        }

        pendingAssignmentsCard = new VerticalLayout();
        pendingAssignmentsCard.setPadding(true);
        pendingAssignmentsCard.setSpacing(true);
        pendingAssignmentsCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        pendingAssignmentsCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        pendingAssignmentsCard.add(new H2("Pending Assignments"));

        refreshPendingAssignments(userId, token);

        add(ownedStoresCard, managedStoresCard, pendingAssignmentsCard);
    }

    private void acceptAssignment(int storeId, int userId, String token, VerticalLayout assignmentCard) {
        String url = String.format(backendUrl + "/api/store/acceptAssignment/%d/%d", storeId, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Span successMessage = new Span("Assignment accepted successfully.");
                successMessage.getStyle().set("color", "green");
                assignmentCard.removeAll();
                assignmentCard.add(successMessage);
                refreshPendingAssignments(userId, token);
            } else {
                Span errorMessage = new Span("Failed to accept assignment: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
                errorMessage.getStyle().set("color", "red");
                assignmentCard.add(errorMessage);
            }
        } catch (Exception ex) {
            Span errorMessage = new Span("Error accepting assignment: " + ex.getMessage());
            errorMessage.getStyle().set("color", "red");
            assignmentCard.add(errorMessage);
        }
    }

    private void declineAssignment(int storeId, int userId, String token, VerticalLayout assignmentCard) {
        String url = String.format(backendUrl + "/api/store/declineAssignment/%d/%d", storeId, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Span successMessage = new Span("Assignment declined successfully.");
                successMessage.getStyle().set("color", "green");
                assignmentCard.removeAll();
                assignmentCard.add(successMessage);
                refreshPendingAssignments(userId, token);
            } else {
                Span errorMessage = new Span("Failed to decline assignment: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
                errorMessage.getStyle().set("color", "red");
                assignmentCard.add(errorMessage);
            }
        } catch (Exception ex) {
            Span errorMessage = new Span("Error declining assignment: " + ex.getMessage());
            errorMessage.getStyle().set("color", "red");
            assignmentCard.add(errorMessage);
        }
    }

    private void refreshPendingAssignments(int userId, String token) {
        pendingAssignmentsCard.removeAll();
        pendingAssignmentsCard.add(new H2("Pending Assignments"));

        String assignmentsUrl = String.format(backendUrl + "/api/user/getAssignmentMessages/%d", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<HashMap<Integer, String>>> response =
                restTemplate.exchange(
                    assignmentsUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                HashMap<Integer, String> assignments = response.getBody().getData();
                if (assignments != null && !assignments.isEmpty()) {
                    for (Map.Entry<Integer, String> entry : assignments.entrySet()) {
                        int storeId = entry.getKey();
                        String message = entry.getValue();
                        VerticalLayout assignmentCard = new VerticalLayout();
                        assignmentCard.setPadding(true);
                        assignmentCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
                        assignmentCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

                        Span assignmentInfo = new Span("Store ID: " + storeId + " | Message: " + message);

                        HorizontalLayout actionButtons = new HorizontalLayout();
                        actionButtons.setSpacing(true);

                        Button acceptButton = new Button("Accept", e -> acceptAssignment(storeId, userId, token, assignmentCard));
                        Button declineButton = new Button("Decline", e -> declineAssignment(storeId, userId, token, assignmentCard));

                        actionButtons.add(acceptButton, declineButton);
                        assignmentCard.add(assignmentInfo, actionButtons);
                        pendingAssignmentsCard.add(assignmentCard);
                    }
                } else {
                    pendingAssignmentsCard.add(new Span("No pending assignments."));
                }
            } else if (response.getStatusCode().value() == 400 && "No messages found".equals(response.getBody().getMessage())) {
                pendingAssignmentsCard.add(new Span("No pending assignments."));
            } else {
                pendingAssignmentsCard.add(new Span("Failed to fetch assignments: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error")));
            }
        } catch (Exception ex) {
            pendingAssignmentsCard.add(new Span("Error fetching assignments: " + ex.getMessage()));
        }
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
            ResponseEntity<Response<Map<Integer, Map<String, String>>>> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            Map<Integer, String> roleMap = new HashMap<>();
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Map<Integer, Map<String, String>> roles = response.getBody().getData();
                if (roles != null) {
                    for (Map.Entry<Integer, Map<String, String>> entry : roles.entrySet()) {
                        Integer storeId = entry.getKey();
                        Map<String, String> roleData = entry.getValue();
                        String type = roleData.get("type");
                        if (type != null) {
                            roleMap.put(storeId, type);
                        } else {
                            Notification.show("Null role type for store ID: " + storeId + ". Skipping this role.", 3000, Notification.Position.MIDDLE);
                        }
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

    private Component createStoreCard(StoreDTO store, String role) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER); // Center content
        layout.getStyle()
            .set("border-radius", "8px")
            .set("padding", "10px")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("cursor", "pointer");
        layout.getElement().addEventListener("mouseover", e ->
            layout.getStyle()
                .set("transform", "scale(1.02)")
                .set("box-shadow", "0 6px 12px rgba(0,0,0,0.15)"));
        layout.getElement().addEventListener("mouseout", e ->
            layout.getStyle()
                .set("transform", "scale(1)")
                .set("box-shadow", "none"));

        RouterLink link = new RouterLink(store.getName(), StoreView.class);
        link.setQueryParameters(QueryParameters.simple(Collections.singletonMap("storeId", String.valueOf(store.getStoreId()))));
        link.getStyle().set("font-size", "1.5em").set("font-weight", "bold").set("text-align", "center");

        HorizontalLayout infoLayout = new HorizontalLayout();
        infoLayout.setSpacing(true);
        infoLayout.setAlignItems(Alignment.CENTER);
        infoLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        infoLayout.add(
            new Span("ID: " + store.getStoreId()),
            new Span("Open: " + (store.isOpen() ? "Yes" : "No")),
            new Span("Avg Rating: " + store.getRatings().values().stream().mapToDouble(d -> d).average().orElse(0.0)),
            new Span("Role: " + role)
        );

        layout.add(link, infoLayout);
        return layout;
    }
}