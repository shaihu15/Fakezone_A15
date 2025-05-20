package com.fakezone.fakezone.ui.view;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "store", layout = MainLayout.class)
public class StoreView extends VerticalLayout implements BeforeEnterObserver{

    private final String backendUrl = "http://localhost:8080";
    private final RestTemplate restTemplate;
    private VerticalLayout storeInfoCard;
    private Button manageButton;

    public StoreView() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER); // Center all content
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        UserDTO userDTO = session != null ? (UserDTO) session.getAttribute("userDTO") : null;

        if (token == null || userDTO == null || isGuestToken(session)) {
            event.rerouteTo("login");
            return;
        }

        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = queryParameters.getParameters();
        List<String> storeIdParam = parameters.getOrDefault("storeId", List.of());

        if (storeIdParam.isEmpty()) {
            add(new Span("No store ID provided."));
            return;
        }

        int storeId;
        try {
            storeId = Integer.parseInt(storeIdParam.get(0));
        } catch (NumberFormatException e) {
            add(new Span("Invalid store ID."));
            return;
        }

        StoreDTO store = getStoreDTO(storeId, token);
        if (store == null) {
            add(new Span("Failed to load store with ID: " + storeId));
            return;
        }

        // Show store info in a centered card (displayed initially)
        storeInfoCard = new VerticalLayout();
        storeInfoCard.setPadding(true);
        storeInfoCard.setSpacing(true);
        storeInfoCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        storeInfoCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        H2 title = new H2("Store Details");
        title.getStyle().set("color", "#2E7D32"); // Green color for title
        storeInfoCard.add(title);
        String storeDetailsLine = String.format(
            "Name: %s  |  ID: %d  |  Open: %s  |  Average Rating: %.1f",
            store.getName(),
            store.getStoreId(),
            store.isOpen() ? "Yes" : "No",
            store.getRatings().values().stream().mapToDouble(d -> d).average().orElse(0.0)
        );
        storeInfoCard.add(new Span(storeDetailsLine));

        // Add "Manage" button
        manageButton = new Button("Manage Store", e -> {
            int userId = userDTO.getUserId(); // Get userId from UserDTO
            fetchAndShowStoreRoles(storeId, userId, token, userDTO);
        });
        add(storeInfoCard, manageButton);
    }

    private void fetchAndShowStoreRoles(int storeId, int userId, String token, UserDTO userDTO) {
        // Clear existing content (hides Store Details and Manage button)
        removeAll();

        String url = String.format(backendUrl + "/api/store/getStoreRoles/%d/%d", storeId, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<StoreRolesDTO>> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                StoreRolesDTO roles = response.getBody().getData();

                // Create a card for roles in columns
                VerticalLayout rolesCard = new VerticalLayout();
                rolesCard.setPadding(true);
                rolesCard.setSpacing(true);
                rolesCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
                rolesCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

                H2 rolesTitle = new H2("Store Roles");
                rolesTitle.getStyle().set("color", "#D81B60"); // Pink color for title
                rolesCard.add(rolesTitle);

                // Use HorizontalLayout for columnar display
                HorizontalLayout rolesLayout = new HorizontalLayout();
                rolesLayout.setSpacing(true);
                rolesLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

                // Column 1: Founder
                VerticalLayout founderColumn = new VerticalLayout();
                founderColumn.setSpacing(false);
                founderColumn.add(new Span("Founder:"), new Span(String.valueOf(roles.getFounderId())));
                rolesLayout.add(founderColumn);

                // Column 2: Owners
                VerticalLayout ownersColumn = new VerticalLayout();
                ownersColumn.setSpacing(false);
                ownersColumn.add(new Span("Owners:"));
                String ownersList = roles.getStoreOwners() != null ? String.join(", ", roles.getStoreOwners().stream().map(String::valueOf).toList()) : "None";
                ownersColumn.add(new Span(ownersList));
                rolesLayout.add(ownersColumn);

                // Column 3: Managers
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

                // Add action buttons inside the Store Roles card
                HorizontalLayout actions = new HorizontalLayout();
                actions.setSpacing(true);
                actions.setAlignItems(Alignment.CENTER);

                Button addManagerButton = new Button("Add Manager");
                addManagerButton.addClickListener(e -> showAddManagerDialog(storeId, userId, token));

                Button addOwnerButton = new Button("Add Owner");
                addOwnerButton.addClickListener(e -> showAddOwnerDialog(storeId, userId, token));

                Button addProductButton = new Button("Add Product");
                addProductButton.addClickListener(e -> showAddProductDialog(storeId, userId, token));

                actions.add(addManagerButton, addOwnerButton, addProductButton);

                rolesCard.add(rolesLayout, actions);

                // Add "Back" button to restore the initial view
                Button backButton = new Button("Back", e -> {
                    removeAll();
                    add(storeInfoCard, manageButton);
                });

                add(rolesCard, backButton);
            } else {
                add(new Span("Failed to fetch store roles: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error")));
            }
        } catch (Exception ex) {
            add(new Span("Error while fetching roles: " + ex.getMessage()));
        }
    }

    private void showAddManagerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        IntegerField managerIdField = new IntegerField("Manager ID");
        CheckboxGroup<StoreManagerPermission> permissionsGroup = new CheckboxGroup<>("Permissions");
        permissionsGroup.setItems(StoreManagerPermission.values());
        permissionsGroup.getStyle().set("display", "flex").set("flex-direction", "column");

        Button confirmButton = new Button("Add", e -> {
            int managerId = managerIdField.getValue() != null ? managerIdField.getValue() : 0;
            List<StoreManagerPermission> permissions = new java.util.ArrayList<>(permissionsGroup.getSelectedItems());
            if (managerId > 0 && !permissions.isEmpty()) {
                addStoreManager(storeId, requesterId, managerId, permissions, token);
                dialog.close();
            } else {
                dialog.add(new Span("Please enter a valid Manager ID and select at least one permission."));
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(managerIdField, permissionsGroup, buttons);
        dialog.open();
    }

    private void showAddOwnerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        IntegerField ownerIdField = new IntegerField("Owner ID");

        Button confirmButton = new Button("Add", e -> {
            int ownerId = ownerIdField.getValue() != null ? ownerIdField.getValue() : 0;
            if (ownerId > 0) {
                addStoreOwner(storeId, requesterId, ownerId, token);
                dialog.close();
            } else {
                dialog.add(new Span("Please enter a valid Owner ID."));
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(ownerIdField, buttons);
        dialog.open();
    }

    private void showAddProductDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        TextField productNameField = new TextField("Product Name");
        TextField descriptionField = new TextField("Description");
        TextField basePriceField = new TextField("Base Price");
        IntegerField quantityField = new IntegerField("Quantity");
        ComboBox<PCategory> categoryField = new ComboBox<>("Category");
        categoryField.setItems(PCategory.values());
        categoryField.setRequired(true);

        Button confirmButton = new Button("Add", e -> {
            String productName = productNameField.getValue();
            String description = descriptionField.getValue();
            double basePrice = basePriceField.getValue() != null && !basePriceField.getValue().isEmpty() ? Double.parseDouble(basePriceField.getValue()) : 0.0;
            int quantity = quantityField.getValue() != null ? quantityField.getValue() : 0;
            PCategory category = categoryField.getValue();
            if (!productName.isEmpty() && !description.isEmpty() && basePrice > 0 && quantity > 0 && category != null) {
                addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category.name(), token);
                dialog.close();
            } else {
                dialog.add(new Span("Please fill all fields with valid values."));
            }
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        dialog.add(productNameField, descriptionField, basePriceField, quantityField, categoryField, buttons);
        dialog.open();
    }

    private void addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> permissions, String token) {
        String url = String.format(backendUrl + "/api/store/addStoreManager/%d/%d/%d", storeId, requesterId, managerId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(
            Map.of("permissions", permissions.stream().map(Enum::name).toList()),
            headers
        );

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                fetchAndShowStoreRoles(storeId, requesterId, token, null); // Refresh roles
            } else {
                add(new Span("Failed to add manager: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error")));
            }
        } catch (Exception ex) {
            add(new Span("Error adding manager: " + ex.getMessage()));
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
                fetchAndShowStoreRoles(storeId, requesterId, token, null); // Refresh roles
            } else {
                add(new Span("Failed to add owner: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error")));
            }
        } catch (Exception ex) {
            add(new Span("Error adding owner: " + ex.getMessage()));
        }
    }

    private void addProductToStore(int storeId, int requesterId, String productName, String description, double basePrice, int quantity, String category, String token) {
        String url = String.format(backendUrl + "/api/store/addProductToStore/%d/%d", storeId, requesterId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
            Map.of(
                "productName", productName,
                "description", description,
                "basePrice", basePrice,
                "quantity", quantity,
                "category", category
            ),
            headers
        );

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                fetchAndShowStoreRoles(storeId, requesterId, token, null); // Refresh roles (though product addition might need a separate view)
            } else {
                add(new Span("Failed to add product: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error")));
            }
        } catch (Exception ex) {
            add(new Span("Error adding product: " + ex.getMessage()));
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

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}