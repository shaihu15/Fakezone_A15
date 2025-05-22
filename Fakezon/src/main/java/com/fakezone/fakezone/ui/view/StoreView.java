package com.fakezone.fakezone.ui.view;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Response;
import DomainLayer.Enums.StoreManagerPermission;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "store", layout = MainLayout.class)
public class StoreView extends VerticalLayout implements BeforeEnterObserver{

    private final String backendUrl = "http://localhost:8080";
    private final RestTemplate restTemplate;
    private final Span loading = new Span("Loading store...");

    public StoreView() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        loading.getStyle().set("color", "#555").set("font-style", "italic");
        add(loading);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();
        add(loading);

        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        UserDTO userDTO = session != null ? (UserDTO) session.getAttribute("userDTO") : null;

        System.out.println("StoreView: token=" + (token != null ? "present" : "null") + ", userId=" + (userDTO != null ? userDTO.getUserId() : "null"));

        if (token == null || userDTO == null || isGuestToken(session)) {
            event.rerouteTo("login");
            Notification.show("Please log in to view this page.", 3000, Notification.Position.MIDDLE);
            remove(loading);
            return;
        }

        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = queryParameters.getParameters();
        List<String> storeIdParam = parameters.getOrDefault("storeId", List.of());

        if (storeIdParam.isEmpty()) {
            remove(loading);
            add(createErrorCard("No store ID provided."));
            Notification.show("No store ID provided.", 3000, Notification.Position.MIDDLE);
            return;
        }

        int storeId;
        try {
            storeId = Integer.parseInt(storeIdParam.get(0));
        } catch (NumberFormatException e) {
            remove(loading);
            add(createErrorCard("Invalid store ID."));
            Notification.show("Invalid store ID.", 3000, Notification.Position.MIDDLE);
            return;
        }

        StoreDTO store = getStoreDTO(storeId, token);
        remove(loading);
        if (store == null) {
            add(createErrorCard("Failed to load store with ID: " + storeId));
            return;
        }

        // Show store info in a centered card
        VerticalLayout storeInfoCard = createStoreInfoCard(store);

        // Add navigation and action buttons
        Button[] manageButton = new Button[1];
        manageButton[0] = new Button("Manage Store", e -> {
            manageButton[0].setEnabled(false); // Disable during fetch
            fetchAndShowStoreRoles(storeId, userDTO.getUserId(), token);
            manageButton[0].setEnabled(true);
        });
        manageButton[0].getStyle().set("background-color", "#2E7D32").set("color", "white");
        RouterLink backLink = new RouterLink("Back to User Area", UserView.class);
        backLink.getStyle().set("margin", "10px").set("color", "#1976D2");
        add(backLink, storeInfoCard, manageButton[0]);
        }

    private VerticalLayout createStoreInfoCard(StoreDTO store) {
        VerticalLayout storeInfoCard = new VerticalLayout();
        storeInfoCard.setPadding(true);
        storeInfoCard.setSpacing(true);
        storeInfoCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px").set("background-color", "#F5F5F5");
        storeInfoCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        H2 title = new H2("Store Details");
        title.getStyle().set("color", "#2E7D32");
        storeInfoCard.add(title);
        String storeDetailsLine = String.format(
            "Name: %s  |  ID: %d  |  Open: %s  |  Average Rating: %.1f",
            store.getName(),
            store.getStoreId(),
            store.isOpen() ? "Yes" : "No",
            store.getRatings().values().stream().mapToDouble(d -> d).average().orElse(0.0)
        );
        Span details = new Span(storeDetailsLine);
        details.getStyle().set("font-size", "1.1em");
        storeInfoCard.add(details);
        return storeInfoCard;
    }

    private VerticalLayout createErrorCard(String message) {
        VerticalLayout errorCard = new VerticalLayout();
        errorCard.setPadding(true);
        errorCard.setSpacing(true);
        errorCard.getStyle().set("border", "1px solid #FFCDD2").set("border-radius", "8px").set("background-color", "#FFEBEE");
        errorCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        Span error = new Span(message);
        error.getStyle().set("color", "#D32F2F").set("font-weight", "bold");
        errorCard.add(error);
        return errorCard;
    }

    private void fetchAndShowStoreRoles(int storeId, int userId, String token) {
        removeAll();
        add(loading);

        // Rebuild store info and buttons
        StoreDTO store = getStoreDTO(storeId, token);
        VerticalLayout storeInfoCard = store != null ? createStoreInfoCard(store) : createErrorCard("Failed to load store details for ID: " + storeId);

        Button manageButton = new Button("Manage Store");
        manageButton.addClickListener(e -> {
            manageButton.setEnabled(false);
            fetchAndShowStoreRoles(storeId, userId, token);
            manageButton.setEnabled(true);
        });
        manageButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        RouterLink backLink = new RouterLink("Back to user Area", UserView.class);
        backLink.getStyle().set("margin", "10px").set("color", "#1976D2");

        String rolesUrl = String.format(backendUrl + "/api/store/getStoreRoles/%d/%d", storeId, userId);
        String pendingManagersUrl = String.format(backendUrl + "/api/store/getPendingManagers/%d/%d", storeId, userId);
        String pendingOwnersUrl = String.format(backendUrl + "/api/store/getPendingOwners/%d/%d", storeId, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Fetch confirmed roles
            ResponseEntity<Response<StoreRolesDTO>> rolesResponse =
                restTemplate.exchange(
                    rolesUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            // Fetch pending managers
            ResponseEntity<Response<List<Integer>>> pendingManagersResponse =
                restTemplate.exchange(
                    pendingManagersUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            // Fetch pending owners
            ResponseEntity<Response<List<Integer>>> pendingOwnersResponse =
                restTemplate.exchange(
                    pendingOwnersUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            remove(loading);

            if (rolesResponse.getStatusCode().is2xxSuccessful() && rolesResponse.getBody().isSuccess()) {
                StoreRolesDTO roles = rolesResponse.getBody().getData();
                List<Integer> pendingManagers = null;
                if (pendingManagersResponse.getStatusCode().is2xxSuccessful() && pendingManagersResponse.getBody().isSuccess()) {
                    pendingManagers = pendingManagersResponse.getBody().getData();
                } else {
                    Span pendingError = new Span("Failed to fetch pending managers: " + (pendingManagersResponse.getBody() != null ? pendingManagersResponse.getBody().getMessage() : "Unknown error"));
                    pendingError.getStyle().set("color", "red");
                    add(createErrorCard(pendingError.getText()));
                    Notification.show(pendingError.getText(), 3000, Notification.Position.MIDDLE);
                    pendingManagers = new ArrayList<>();
                }

                List<Integer> pendingOwners = null;
                if (pendingOwnersResponse.getStatusCode().is2xxSuccessful() && pendingOwnersResponse.getBody().isSuccess()) {
                    pendingOwners = pendingOwnersResponse.getBody().getData();
                } else {
                    Span pendingError = new Span("Failed to fetch pending owners: " + (pendingOwnersResponse.getBody() != null ? pendingOwnersResponse.getBody().getMessage() : "Unknown error"));
                    pendingError.getStyle().set("color", "red");
                    add(createErrorCard(pendingError.getText()));
                    Notification.show(pendingError.getText(), 3000, Notification.Position.MIDDLE);
                    pendingOwners = new ArrayList<>();
                }

                // Create roles card
                VerticalLayout rolesCard = createRolesCard(roles, pendingManagers, pendingOwners, storeId, userId, token);

                add(backLink, storeInfoCard, manageButton, rolesCard);
            } else {
                add(backLink, storeInfoCard, manageButton, createErrorCard("Failed to fetch store roles: " + (rolesResponse.getBody() != null ? rolesResponse.getBody().getMessage() : "Unknown error")));
                Notification.show("Failed to fetch store roles.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            remove(loading);
            add(backLink, storeInfoCard, manageButton, createErrorCard("Error while fetching roles: " + ex.getMessage()));
            Notification.show("Error fetching roles: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private VerticalLayout createRolesCard(StoreRolesDTO roles, List<Integer> pendingManagers, List<Integer> pendingOwners, int storeId, int userId, String token) {
        VerticalLayout rolesCard = new VerticalLayout();
        rolesCard.setPadding(true);
        rolesCard.setSpacing(true);
        rolesCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px").set("background-color", "#F5F5F5");
        rolesCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H2 rolesTitle = new H2("Store Roles");
        rolesTitle.getStyle().set("color", "#D81B60");
        rolesCard.add(rolesTitle);

        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        rolesLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // Founder
        VerticalLayout founderColumn = new VerticalLayout();
        founderColumn.setSpacing(false);
        founderColumn.add(new Span("Founder:"), new Span(String.valueOf(roles.getFounderId())));
        rolesLayout.add(founderColumn);

        // Owners
        VerticalLayout ownersColumn = new VerticalLayout();
        ownersColumn.setSpacing(false);
        ownersColumn.add(new Span("Owners:"));
        String ownersList = roles.getStoreOwners() != null ? String.join(", ", roles.getStoreOwners().stream().map(String::valueOf).toList()) : "None";
        ownersColumn.add(new Span(ownersList));
        rolesLayout.add(ownersColumn);

        // Managers
        VerticalLayout managersColumn = new VerticalLayout();
        managersColumn.setSpacing(false);
        managersColumn.add(new Span("Managers:"));
        Map<Integer, List<StoreManagerPermission>> managers = roles.getStoreManagers();
        if (managers != null && !managers.isEmpty()) {
            managers.forEach((managerId, permissions) -> {
                String formattedPermissions = permissions != null ? String.join(", ", permissions.stream().map(Enum::name).toList()) : "None";
                managersColumn.add(new Span("ID: " + managerId + " → " + formattedPermissions));
            });
        } else {
            managersColumn.add(new Span("None"));
        }
        rolesLayout.add(managersColumn);

        // Pending Managers
        VerticalLayout pendingManagersColumn = new VerticalLayout();
        pendingManagersColumn.setSpacing(false);
        pendingManagersColumn.add(new Span("Pending Managers:"));
        if (pendingManagers != null && !pendingManagers.isEmpty()) {
            pendingManagers.forEach(managerId -> {
                pendingManagersColumn.add(new Span("ID: " + managerId + " (awaiting approval)"));
            });
        } else {
            pendingManagersColumn.add(new Span("None"));
        }
        rolesLayout.add(pendingManagersColumn);

        // Pending Owners
        VerticalLayout pendingOwnersColumn = new VerticalLayout();
        pendingOwnersColumn.setSpacing(false);
        pendingOwnersColumn.add(new Span("Pending Owners:"));
        if (pendingOwners != null && !pendingOwners.isEmpty()) {
            pendingOwners.forEach(ownerId -> {
                pendingOwnersColumn.add(new Span("ID: " + ownerId + " (awaiting approval)"));
            });
        } else {
            pendingOwnersColumn.add(new Span("None"));
        }
        rolesLayout.add(pendingOwnersColumn);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setAlignItems(Alignment.CENTER);
        Button addManagerButton = new Button("Add Manager", e -> showAddManagerDialog(storeId, userId, token));
        addManagerButton.getStyle().set("background-color", "#1976D2").set("color", "white");
        Button addOwnerButton = new Button("Add Owner", e -> showAddOwnerDialog(storeId, userId, token));
        addOwnerButton.getStyle().set("background-color", "#1976D2").set("color", "white");
        Button addProductButton = new Button("Add Product", e -> showAddProductDialog(storeId, userId, token));
        addProductButton.getStyle().set("background-color", "#1976D2").set("color", "white");
        actions.add(addManagerButton, addOwnerButton, addProductButton);

        rolesCard.add(rolesLayout, actions);
        return rolesCard;
    }

    private void showAddManagerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        IntegerField managerIdField = new IntegerField("Manager ID");
        managerIdField.setRequiredIndicatorVisible(true);
        CheckboxGroup<StoreManagerPermission> permissionsGroup = new CheckboxGroup<>("Permissions");
        permissionsGroup.setItems(StoreManagerPermission.values());
        permissionsGroup.getStyle().set("display", "flex").set("flex-direction", "column");
        permissionsGroup.setRequired(true);

        Span errorMessage = new Span();
        errorMessage.getStyle().set("color", "red").set("display", "none");

        Button confirmButton = new Button("Add", e -> {
            Integer managerId = managerIdField.getValue();
            List<StoreManagerPermission> permissions = new ArrayList<>(permissionsGroup.getSelectedItems());
            if (managerId == null || managerId <= 0 || permissions.isEmpty()) {
                errorMessage.setText("Please enter a valid Manager ID and select at least one permission.");
                errorMessage.getStyle().set("display", "block");
                return;
            }
            addStoreManager(storeId, requesterId, managerId, permissions, token);
            dialog.close();
        });
        confirmButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#D32F2F").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(managerIdField, permissionsGroup, errorMessage, buttons);
        dialog.open();
    }

    private void showAddOwnerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        IntegerField ownerIdField = new IntegerField("Owner ID");
        ownerIdField.setRequiredIndicatorVisible(true);

        Span errorMessage = new Span();
        errorMessage.getStyle().set("color", "red").set("display", "none");

        Button confirmButton = new Button("Add", e -> {
            Integer ownerId = ownerIdField.getValue();
            if (ownerId == null || ownerId <= 0) {
                errorMessage.setText("Please enter a valid Owner ID.");
                errorMessage.getStyle().set("display", "block");
                return;
            }
            addStoreOwner(storeId, requesterId, ownerId, token);
            dialog.close();
        });
        confirmButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#D32F2F").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(ownerIdField, errorMessage, buttons);
        dialog.open();
    }

    private void showAddProductDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        TextField productNameField = new TextField("Product Name");
        productNameField.setRequiredIndicatorVisible(true);
        TextField descriptionField = new TextField("Description");
        descriptionField.setRequiredIndicatorVisible(true);
        TextField basePriceField = new TextField("Base Price");
        basePriceField.setRequiredIndicatorVisible(true);
        IntegerField quantityField = new IntegerField("Quantity");
        quantityField.setRequiredIndicatorVisible(true);
        ComboBox<PCategory> categoryField = new ComboBox<>("Category");
        categoryField.setItems(PCategory.values());
        categoryField.setRequired(true);

        Span errorMessage = new Span();
        errorMessage.getStyle().set("color", "red").set("display", "none");

        Button confirmButton = new Button("Add", e -> {
            String productName = productNameField.getValue();
            String description = descriptionField.getValue();
            double basePrice;
            try {
                basePrice = basePriceField.getValue() != null && !basePriceField.getValue().isEmpty() ? Double.parseDouble(basePriceField.getValue()) : 0.0;
            } catch (NumberFormatException ex) {
                errorMessage.setText("Invalid base price.");
                errorMessage.getStyle().set("display", "block");
                return;
            }
            Integer quantity = quantityField.getValue();
            PCategory category = categoryField.getValue();
            if (productName.isEmpty() || description.isEmpty() || basePrice <= 0 || quantity == null || quantity <= 0 || category == null) {
                errorMessage.setText("Please fill all fields with valid values.");
                errorMessage.getStyle().set("display", "block");
                return;
            }
            addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category.name(), token);
            dialog.close();
        });
        confirmButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#D32F2F").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(productNameField, descriptionField, basePriceField, quantityField, categoryField, errorMessage, buttons);
        dialog.open();
    }

    private void addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> permissions, String token) {
        String baseUrl = String.format(
            backendUrl + "/api/store/addStoreManager/%d/%d/%d",
            storeId, requesterId, managerId
        );
        StringBuilder queryParams = new StringBuilder();
        queryParams.append("?");
        for (int i = 0; i < permissions.size(); i++) {
            if (i > 0) {
                queryParams.append("&");
            }
            queryParams.append("permissions=")
                    .append(URLEncoder.encode(permissions.get(i).name(), StandardCharsets.UTF_8));
        }
        String url = baseUrl + queryParams.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Manager added successfully.", 3000, Notification.Position.MIDDLE);
                fetchAndShowStoreRoles(storeId, requesterId, token);
            } else {
                Notification.show("Failed to add manager: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                    3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding manager: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void addStoreOwner(int storeId, int requesterId, int ownerId, String token) {
        String url = String.format(backendUrl + "/api/store/addStoreOwner/%d/%d/%d", storeId, requesterId, ownerId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Owner added successfully.", 3000, Notification.Position.MIDDLE);
                fetchAndShowStoreRoles(storeId, requesterId, token);
            } else {
                Notification.show("Failed to add owner: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                    3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding owner: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void addProductToStore(int storeId, int requesterId, String productName, String description, double basePrice, int quantity, String category, String token) {
        String url = String.format(
            backendUrl + "/api/store/addProductToStore/%d/%d?productName=%s&description=%s&basePrice=%f&quantity=%d&category=%s",
            storeId, requesterId,
            URLEncoder.encode(productName, StandardCharsets.UTF_8),
            URLEncoder.encode(description, StandardCharsets.UTF_8),
            basePrice,
            quantity,
            URLEncoder.encode(category, StandardCharsets.UTF_8)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Product added successfully.", 3000, Notification.Position.MIDDLE);
                fetchAndShowStoreRoles(storeId, requesterId, token);
            } else {
                Notification.show("Failed to add product: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                    3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding product: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
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
            return true;
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

            System.out.println("getStoreDTO for storeId=" + storeId + ": Status=" + response.getStatusCode() +
                                ", Success=" + (response.getBody() != null ? response.getBody().isSuccess() : "null") +
                                ", Message=" + (response.getBody() != null ? response.getBody().getMessage() : "null"));

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                Notification.show("Failed to load store with ID: " + storeId + ". Reason: " +
                                    (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                    3000, Notification.Position.MIDDLE);
                return null;
            }
        } catch (Exception e) {
            System.err.println("getStoreDTO exception for storeId=" + storeId + ": " + e.getMessage());
            Notification.show("Error fetching store with ID: " + storeId + ": " + e.getMessage(),
                                3000, Notification.Position.MIDDLE);
            return null;
        }
    }
}