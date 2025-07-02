package com.fakezone.fakezone.ui.view;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.OrderedProductDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.OrderedProduct;
import DomainLayer.Model.helpers.UserMsg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "store/:storeId/manage", layout = MainLayout.class)
public class StoreManageView extends VerticalLayout implements BeforeEnterObserver{

    private final String apiUrl;
    private final RestTemplate restTemplate;
    private final Span loading = new Span("Loading store...");
    private final String webUrl;
    // Store these at the class level after initial fetch
    private int currentStoreId;
    private UserDTO currentUserDTO;
    private String currentToken;
    private Set<StoreManagerPermission> effectivePermissions = new HashSet<>();
    private VerticalLayout rolesDisplaySection; // New section for roles details
    private int storeId;
    private boolean isOwner;
    private boolean isManager;
    public StoreManageView(@Value("${api.url}") String apiUrl, @Value("${website.url}") String websiteUrl) {
        this.webUrl = websiteUrl;
        this.apiUrl = apiUrl;
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        loading.getStyle().set("color", "#555").set("font-style", "italic");
        add(loading);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        Optional<String> optStoreId = params.get("storeId");
        if(optStoreId.isPresent()){
            this.storeId = Integer.parseInt(optStoreId.get());
        }
        else{
            event.rerouteTo(""); // go to home if no storeId
        }
        this.isOwner = isOwner();
        if(isOwner){
            this.isManager = true;
        }
        else{
            this.isManager = isManager();
        }
        if(!isManager && !isOwner){
            event.rerouteTo("");
            UI.getCurrent().navigate("");
        }
        removeAll();
        add(loading);
        

        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        UserDTO userDTO = session != null ? (UserDTO) session.getAttribute("userDTO") : null;

        System.out.println("StoreManageView: token=" + (token != null ? "present" : "null") + ", userId=" + (userDTO != null ? userDTO.getUserId() : "null"));

        if (token == null || userDTO == null) {
            event.rerouteTo(""); // route to home (shouldn't happen)
            Notification.show("Please log in to view this page.", 3000, Notification.Position.MIDDLE);
            remove(loading);
            return;
        }

        this.currentUserDTO = userDTO;
        this.currentToken = token;
        

        this.currentStoreId = storeId; // Store storeId

        StoreDTO store = getStoreDTO(currentStoreId, currentToken);
        remove(loading);
        if (store == null) {
            add(createErrorCard("Failed to load store with ID: " + currentStoreId));
            return;
        }

        if(!store.isOpen() && store.getFounderId() != currentUserDTO.getUserId()){
            event.rerouteTo("");
            Notification.show("Store is Closed - Contact Store Founder");
        }

        // --- Fetch permissions immediately ---
        fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);

        // Show store info in a centered card
        VerticalLayout storeInfoCard = createStoreInfoCard(store);

        // Action Buttons (visible if permission exists)
        HorizontalLayout actionButtonsLayout = new HorizontalLayout();
        actionButtonsLayout.setSpacing(true);
        actionButtonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        actionButtonsLayout.setAlignItems(Alignment.CENTER);
        actionButtonsLayout.getStyle().set("margin-top", "10px");
        actionButtonsLayout.setWidth("100%"); 
        actionButtonsLayout.setWidth("100%");                      // full‑width container
        actionButtonsLayout.getStyle()
            .set("display", "flex")                                // ensure it’s flex
            .set("flex-wrap", "wrap")                              // allow wrapping
            .set("justify-content", "center")                      // center rows
            .set("align-items", "center")
            .set("gap", "10px")                                     // spacing between items
            .set("margin-top", "10px");

        // INVENTORY buttons
        if (effectivePermissions.contains(StoreManagerPermission.INVENTORY)) {
            //ADD PRODUCT
            Button addProductButton = new Button("Add Product", VaadinIcon.PLUS.create());
            addProductButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            addProductButton.addClickListener(e -> showAddProductDialog(currentStoreId, currentUserDTO.getUserId(), currentToken));
            actionButtonsLayout.add(addProductButton);
            //ADD AUCTION PRODUCT
            Button addAuctionProductButton = new Button("Add Auction Product", VaadinIcon.PLUS.create());
            addAuctionProductButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            addAuctionProductButton.addClickListener(e -> showAddAuctionProductDialog(currentStoreId, currentUserDTO.getUserId(), currentToken));
            actionButtonsLayout.add(addAuctionProductButton);
            //REMOVE PRODUCT
            Button removeProductButton = new Button("Remove Product", VaadinIcon.MINUS.create());
            removeProductButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            removeProductButton.addClickListener(e -> removeProductDialog());
            actionButtonsLayout.add(removeProductButton);
            //EDIT PRODUCT
            Button editProductButton = new Button("Edit Product", VaadinIcon.PENCIL.create());
            editProductButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            editProductButton.addClickListener(e -> editProductDialog());
            actionButtonsLayout.add(editProductButton);

        }
        // Add Manager & Add Owner button
        if (isOwner) { 
            Button addManagerButton = new Button("Add Manager", VaadinIcon.PLUS.create());
            addManagerButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            addManagerButton.addClickListener(e -> showAddManagerDialog(currentStoreId, currentUserDTO.getUserId(), currentToken));
            actionButtonsLayout.add(addManagerButton);
            Button addOwnerButton = new Button("Add Owner", VaadinIcon.PLUS.create());
            addOwnerButton.getStyle().set("background-color", "#1976D2").set("color", "white");
            addOwnerButton.addClickListener(e -> showAddOwnerDialog(currentStoreId, currentUserDTO.getUserId(), currentToken));
            actionButtonsLayout.add(addOwnerButton);
        }

        // Manage Discounts button
        if (effectivePermissions.contains(StoreManagerPermission.DISCOUNT_POLICY)) {
            Button manageDiscountsButton = new Button("Manage Discounts", VaadinIcon.DOLLAR.create());
            manageDiscountsButton.getStyle().set("background-color", "#FF9800").set("color", "white");
            manageDiscountsButton.addClickListener(e -> 
                getUI().ifPresent(ui -> ui.navigate(DiscountView.class, 
                    new RouteParameters("storeId", String.valueOf(currentStoreId)))));
            actionButtonsLayout.add(manageDiscountsButton);
        }

        // "View/Manage Roles" button
        if(effectivePermissions.contains(StoreManagerPermission.VIEW_ROLES)){
            Button viewManageRolesButton = new Button("View Roles", e -> showStoreManagementSection());
            viewManageRolesButton.getStyle().set("background-color", "#2E7D32").set("color", "white");
            actionButtonsLayout.add(viewManageRolesButton);
        }

        if(effectivePermissions.contains(StoreManagerPermission.REQUESTS_REPLY)){
            Button viewMsgs = new Button("View Messages", e -> showMsgs());
            viewMsgs.getStyle().set("background-color", "#2E7D32").set("color", "white");
            actionButtonsLayout.add(viewMsgs);
        }

        if(effectivePermissions.contains(StoreManagerPermission.VIEW_PURCHASES)){
            Button viewPurchasesButton = new Button ("View Purchases");
            viewPurchasesButton.getStyle().set("background-color", "#2E7D32").set("color", "white");
            viewPurchasesButton.addClickListener(e -> viewPurchases());
            actionButtonsLayout.add(viewPurchasesButton);
        }

        if(store.getFounderId() == currentUserDTO.getUserId()){
            if(store.isOpen()){
                Button closeStoreButton = new Button("Close Store");
                closeStoreButton.getStyle().set("background-color", "#c22a2a").set("color", "white");
                closeStoreButton.addClickListener(e -> closeStoreDialog());
                actionButtonsLayout.add(closeStoreButton);
            }
            else{
                Button openStoreButton = new Button("Open Store");
                openStoreButton.getStyle().set("background-color", "#c22a2a").set("color", "white");
                openStoreButton.addClickListener(e -> openStoreDialog());
                actionButtonsLayout.add(openStoreButton);
            }
        }
    

        RouterLink backLink = new RouterLink("Back to Store", StorePageView.class, new RouteParameters("storeId", String.valueOf(this.storeId)));        
        backLink.getStyle().set("margin", "10px").set("color", "#1976D2");

        // Initialize rolesDisplaySection, initially empty
        rolesDisplaySection = new VerticalLayout();
        rolesDisplaySection.setPadding(false);
        rolesDisplaySection.setSpacing(true);
        rolesDisplaySection.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(backLink, storeInfoCard, actionButtonsLayout, rolesDisplaySection);
        
    }

    private void fetchUserPermissionsForStore(int storeId, int userId, String token) {
        if(!isOwner()){
            isManager();
        }
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
                "Name: %s | ID: %d | Open: %s | Average Rating: %.1f",
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

    private void showStoreManagementSection() {
        rolesDisplaySection.removeAll(); // Clear previous content

        // Fetch full roles data (this is where the backend fix for getStoreRoles is critical)
        String rolesUrl = String.format(apiUrl + "store/getStoreRoles/%d/%d", currentStoreId, currentUserDTO.getUserId());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        StoreRolesDTO roles = null;
        try {
            ResponseEntity<Response<StoreRolesDTO>> rolesResponse =
                restTemplate.exchange(
                    rolesUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            if (rolesResponse.getStatusCode().is2xxSuccessful() && rolesResponse.getBody().isSuccess()) {
                roles = rolesResponse.getBody().getData();
            } else {
                // If backend returns an error or success=false for getStoreRoles,
                // we still proceed but roles will be null, and specific sections will be hidden.
                Notification.show("Failed to fetch full store roles for display: " +
                                  (rolesResponse.getBody() != null ? rolesResponse.getBody().getMessage() : "Unknown error"),
                                  3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error fetching full store roles for display: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            // roles will remain null, leading to conditional display
        }


        // These will be empty if effectivePermissions doesn't contain VIEW_ROLES
        List<Integer> pendingManagers = new ArrayList<>();
        List<Integer> pendingOwners = new ArrayList<>();
        Component pendingRolesMessage = null;

        boolean canViewPendingRoles = effectivePermissions.contains(StoreManagerPermission.VIEW_ROLES);

        if (canViewPendingRoles) {
            // Only attempt to fetch pending if user has VIEW_ROLES
            String pendingManagersUrl = String.format(apiUrl + "store/getPendingManagers/%d/%d", currentStoreId, currentUserDTO.getUserId());
            String pendingOwnersUrl = String.format(apiUrl + "store/getPendingOwners/%d/%d", currentStoreId, currentUserDTO.getUserId());

            try {
                ResponseEntity<Response<List<Integer>>> pendingManagersResponse =
                    restTemplate.exchange(
                        pendingManagersUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                    );
                if (pendingManagersResponse.getStatusCode().is2xxSuccessful() && pendingManagersResponse.getBody().isSuccess()) {
                    pendingManagers = pendingManagersResponse.getBody().getData();
                } else {
                    Notification.show("Failed to fetch pending managers: " + (pendingManagersResponse.getBody() != null ? pendingManagersResponse.getBody().getMessage() : "Unknown error"),
                            3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Error fetching pending managers: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }

            try {
                ResponseEntity<Response<List<Integer>>> pendingOwnersResponse =
                    restTemplate.exchange(
                        pendingOwnersUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<>() {}
                    );
                if (pendingOwnersResponse.getStatusCode().is2xxSuccessful() && pendingOwnersResponse.getBody().isSuccess()) {
                    pendingOwners = pendingOwnersResponse.getBody().getData();
                } else {
                    Notification.show("Failed to fetch pending owners: " + (pendingOwnersResponse.getBody() != null ? pendingOwnersResponse.getBody().getMessage() : "Unknown error"),
                            3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Error fetching pending owners: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        } else {
            // Set a message to display in place of pending roles if user has no permission
            Span message = new Span("You do not have permission to view pending roles or manage existing roles.");
            message.getStyle().set("color", "#757575").set("font-style", "italic");
            pendingRolesMessage = message;
        }

        // Create roles card, passing effective permissions and pending roles message
        VerticalLayout rolesCard = createRolesCard(roles, pendingManagers, pendingOwners, currentStoreId, currentUserDTO.getUserId(), currentToken, effectivePermissions, pendingRolesMessage);
        rolesDisplaySection.add(rolesCard);
    }

    private VerticalLayout createRolesCard(StoreRolesDTO roles, List<Integer> pendingManagers, List<Integer> pendingOwners, int storeId, int userId, String token, Set<StoreManagerPermission> effectivePermissions, Component pendingRolesMessage) {
        VerticalLayout rolesCard = new VerticalLayout();
        rolesCard.setPadding(true);
        rolesCard.setSpacing(true);
        rolesCard.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px").set("background-color", "#F5F5F5");
        rolesCard.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        rolesCard.getStyle().set("margin-top", "20px"); // Add some top margin

        H2 rolesTitle = new H2("Store Roles Details");
        rolesTitle.getStyle().set("color", "#D81B60");
        rolesCard.add(rolesTitle);

        if (roles == null) {
            rolesCard.add(new Span("Could not load roles information."));
            return rolesCard;
        }

        HorizontalLayout rolesLayout = new HorizontalLayout();
        rolesLayout.setSpacing(true);
        rolesLayout.setDefaultVerticalComponentAlignment(Alignment.START);

        // Founder
        VerticalLayout founderColumn = new VerticalLayout();
        founderColumn.setSpacing(false);
        founderColumn.add(new Span("Founder:"), new Span(String.valueOf(roles.getFounderId())));
        rolesLayout.add(founderColumn);

        // Owners
        VerticalLayout ownersColumn = new VerticalLayout();
        ownersColumn.setSpacing(false);
        ownersColumn.add(new Span("Owners:"));
        Collection<Integer> owners = roles.getStoreOwners();
        if (owners != null && !owners.isEmpty()) {
            for (Integer ownerId : owners) {
                HorizontalLayout ownerEntry = new HorizontalLayout();
                ownerEntry.setAlignItems(Alignment.CENTER);
                ownerEntry.add(new Span("ID: " + ownerId));

                // Conditionally add "Remove Owner" button based on ownership
                if (isOwner) {
                    Button removeOwnerButton = new Button("Remove", VaadinIcon.MINUS.create());
                    removeOwnerButton.addThemeName("small error");
                    removeOwnerButton.getStyle().set("margin-left", "10px");
                    int finalOwnerId = ownerId;
                    removeOwnerButton.addClickListener(e -> {
                        showRemoveOwnerConfirmationDialog(storeId, userId, finalOwnerId, token);
                    });
                    ownerEntry.add(removeOwnerButton);
                }
                ownersColumn.add(ownerEntry);
            }
        } else {
            ownersColumn.add(new Span("None"));
        }
        rolesLayout.add(ownersColumn);

        // Managers
        VerticalLayout managersColumn = new VerticalLayout();
        managersColumn.setSpacing(false);
        managersColumn.add(new Span("Managers:"));
        Map<Integer, List<StoreManagerPermission>> managers = roles.getStoreManagers();
        if (managers != null && !managers.isEmpty()) {
            managers.forEach((managerId, managerPermissions) -> {
                String formattedPermissions = managerPermissions != null ? String.join(", ", managerPermissions.stream().map(Enum::name).toList()) : "None";
                HorizontalLayout managerEntry = new HorizontalLayout();
                managerEntry.setAlignItems(Alignment.CENTER);
                managerEntry.add(new Span("ID: " + managerId + " → " + formattedPermissions));

                // Conditionally add "Manage Permissions" & "REMOVE" button based on ownership
                if (isOwner) {
                    Button managePermissionsButton = new Button("Manage Permissions", VaadinIcon.EDIT.create());
                    managePermissionsButton.addThemeName("small");
                    managePermissionsButton.getStyle().set("margin-left", "10px");
                    managePermissionsButton.addClickListener(e -> {
                        showManageManagerPermissionsDialog(storeId, userId, managerId, managerPermissions, token);
                    });
                    managerEntry.add(managePermissionsButton);
                    Button removeManagerButton = new Button("Remove", VaadinIcon.MINUS.create());
                    removeManagerButton.addThemeName("small error");
                    removeManagerButton.getStyle().set("margin-left", "10px");
                    int finalManagerId = managerId;
                    removeManagerButton.addClickListener(e -> {
                        showRemoveManagerConfirmationDialog(storeId, userId, finalManagerId, token);
                    });
                    managerEntry.add(removeManagerButton);
                }
                managersColumn.add(managerEntry);
            });
        } else {
            managersColumn.add(new Span("None"));
        }
        rolesLayout.add(managersColumn);

        // Pending Managers (Conditional Display)
        VerticalLayout pendingManagersColumn = new VerticalLayout();
        pendingManagersColumn.setSpacing(false);
        pendingManagersColumn.add(new Span("Pending Managers:"));
        if (pendingRolesMessage != null) { // Check if the general permission message exists
            pendingManagersColumn.add(pendingRolesMessage);
        } else if (pendingManagers != null && !pendingManagers.isEmpty()) {
            pendingManagers.forEach(managerId -> {
                pendingManagersColumn.add(new Span("ID: " + managerId + " (awaiting approval)"));
            });
        } else {
            pendingManagersColumn.add(new Span("None"));
        }
        rolesLayout.add(pendingManagersColumn);

        // Pending Owners (Conditional Display)
        VerticalLayout pendingOwnersColumn = new VerticalLayout();
        pendingOwnersColumn.setSpacing(false);
        pendingOwnersColumn.add(new Span("Pending Owners:"));
        if (pendingRolesMessage != null) { // Check if the general permission message exists
            pendingOwnersColumn.add(pendingRolesMessage);
        } else if (pendingOwners != null && !pendingOwners.isEmpty()) {
            pendingOwners.forEach(ownerId -> {
                pendingOwnersColumn.add(new Span("ID: " + ownerId + " (awaiting approval)"));
            });
            } else {
            pendingOwnersColumn.add(new Span("None"));
        }
        rolesLayout.add(pendingOwnersColumn);

        rolesCard.add(rolesLayout); // Action buttons are now separate
        return rolesCard;
    }

    // --- NEW METHOD FOR REMOVING STORE OWNER ---
    private void showRemoveOwnerConfirmationDialog(int storeId, int requesterId, int ownerId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("350px");

        H2 title = new H2("Confirm Removal");
        title.getStyle().set("color", "#D32F2F"); // Red for warning

        Span message = new Span("Are you sure you want to remove owner ID " + ownerId + "? This action cannot be undone.");
        message.getStyle().set("text-align", "center");

        Button confirmButton = new Button("Confirm Remove", e -> {
            callRemoveStoreOwnerApi(storeId, requesterId, ownerId, token, dialog);
        });
        confirmButton.getStyle().set("background-color", "#D32F2F").set("color", "white"); // Red button

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#616161").set("color", "white"); // Gray color

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER); // Center buttons

        VerticalLayout layout = new VerticalLayout(title, message, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void callRemoveStoreOwnerApi(int storeId, int requesterId, int ownerId, String token, Dialog dialogToClose) {
        String url = String.format(apiUrl + "store/removeStoreOwner/%d/%d/%d", storeId, requesterId, ownerId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Owner ID " + ownerId + " removed successfully.", 3000, Notification.Position.MIDDLE);
                dialogToClose.close();
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();
            } else {
                Notification.show("Failed to remove owner: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                        3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error removing owner: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    // --- NEW METHOD FOR REMOVING STORE MANAGER ---
    private void showRemoveManagerConfirmationDialog(int storeId, int requesterId, int managerId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("350px");

        H2 title = new H2("Confirm Removal");
        title.getStyle().set("color", "#D32F2F"); // Red for warning

        Span message = new Span("Are you sure you want to remove manager ID " + managerId + "? This will revoke all their permissions.");
        message.getStyle().set("text-align", "center");

        Button confirmButton = new Button("Confirm Remove", e -> {
            callRemoveStoreManagerApi(storeId, requesterId, managerId, token, dialog);
        });
        confirmButton.getStyle().set("background-color", "#D32F2F").set("color", "white"); // Red button

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#616161").set("color", "white"); // Gray color

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(title, message, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void callRemoveStoreManagerApi(int storeId, int requesterId, int managerId, String token, Dialog dialogToClose) {
        String url = String.format(apiUrl + "store/removeStoreManager/%d/%d/%d", storeId, requesterId, managerId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Manager ID " + managerId + " removed successfully.", 3000, Notification.Position.MIDDLE);
                dialogToClose.close();
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();
            } else {
                Notification.show("Failed to remove manager: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                        3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error removing manager: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    // --- Permissions Management Dialog and API Calls ---

    private void showManageManagerPermissionsDialog(int storeId, int userId, int managerId, List<StoreManagerPermission> currentPermissions, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("500px");

        H2 title = new H2("Manage Permissions for Manager ID: " + managerId);
        title.getStyle().set("color", "#008CBA");

        Span currentPermissionsSpan = new Span("Current Permissions: " + (currentPermissions != null && !currentPermissions.isEmpty() ? String.join(", ", currentPermissions.stream().map(Enum::name).toList()) : "None"));
        currentPermissionsSpan.getStyle().set("font-style", "italic");

        // CheckboxGroup for adding permissions
        CheckboxGroup<StoreManagerPermission> addPermissionsGroup = new CheckboxGroup<>("Add Permissions");
        Set<StoreManagerPermission> allPermissions = new HashSet<>(Set.of(StoreManagerPermission.values()));
        if (currentPermissions != null) {
            allPermissions.removeAll(currentPermissions); // Only show permissions that can be added
        }
        addPermissionsGroup.setItems(allPermissions);
        addPermissionsGroup.getStyle().set("display", "flex").set("flex-direction", "column");

        // CheckboxGroup for removing permissions
        CheckboxGroup<StoreManagerPermission> removePermissionsGroup = new CheckboxGroup<>("Remove Permissions");
        removePermissionsGroup.setItems(currentPermissions != null ? currentPermissions : new ArrayList<>()); // Only show current permissions to remove
        removePermissionsGroup.getStyle().set("display", "flex").set("flex-direction", "column");

        Span errorMessage = new Span();
        errorMessage.getStyle().set("color", "red").set("display", "none");

        Button saveButton = new Button("Save Changes", e -> {
            Set<StoreManagerPermission> permissionsToAdd = addPermissionsGroup.getSelectedItems();
            Set<StoreManagerPermission> permissionsToRemove = removePermissionsGroup.getSelectedItems();

            boolean changesMade = false;
            // Handle adding permissions
            if (!permissionsToAdd.isEmpty()) {
                callAddStoreManagerPermissionsApi(storeId, managerId, new ArrayList<>(permissionsToAdd), token, dialog);
                changesMade = true;
            }

            // Handle removing permissions
            if (!permissionsToRemove.isEmpty()) {
                callRemoveStoreManagerPermissionsApi(storeId, managerId, new ArrayList<>(permissionsToRemove), token, dialog);
                changesMade = true;
            }

            if (!changesMade) {
                Notification.show("No changes to save.", 3000, Notification.Position.MIDDLE);
                dialog.close();
            }
            // If changes were made, the API calls will refresh the roles and close the dialog
        });
        saveButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#616161").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(title, currentPermissionsSpan, addPermissionsGroup, removePermissionsGroup, errorMessage, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void callAddStoreManagerPermissionsApi(int storeId, int managerId, List<StoreManagerPermission> permissionsToAdd, String token, Dialog dialogToClose) {
        if (permissionsToAdd.isEmpty()) {
            Notification.show("No permissions selected to add.", 3000, Notification.Position.MIDDLE);
            return;
        }

        String baseUrl = String.format(
                apiUrl + "store/addStoreManagerPermissions/%d/%d",
                storeId, managerId
        );
        StringBuilder queryParams = new StringBuilder();
        queryParams.append("?");
        for (int i = 0; i < permissionsToAdd.size(); i++) {
            if (i > 0) {
                queryParams.append("&");
            }
            queryParams.append("permissions=")
                    .append(URLEncoder.encode(permissionsToAdd.get(i).name(), StandardCharsets.UTF_8));
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
                Notification.show("Permissions added successfully.", 3000, Notification.Position.MIDDLE);
                dialogToClose.close();
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();
            } else {
                Notification.show("Failed to add permissions: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding permissions: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void callRemoveStoreManagerPermissionsApi(int storeId, int managerId, List<StoreManagerPermission> permissionsToRemove, String token, Dialog dialogToClose) {
        if (permissionsToRemove == null || permissionsToRemove.isEmpty()) {
            Notification.show("No permissions selected to remove.", 3000, Notification.Position.MIDDLE);
            return;
        }

        StringBuilder urlBuilder = new StringBuilder(String.format(
                apiUrl + "store/removeStoreManagerPermissions/%d/%d",
                storeId, managerId
        ));
        urlBuilder.append("?");
        for (int i = 0; i < permissionsToRemove.size(); i++) {
            if (i > 0) {
                urlBuilder.append("&");
            }
            urlBuilder.append("permissions=").append(URLEncoder.encode(permissionsToRemove.get(i).name(), StandardCharsets.UTF_8));
        }
        String url = urlBuilder.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Notification.show("Permissions removed successfully.", 3000, Notification.Position.MIDDLE);
                dialogToClose.close();
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();

            } else {
                Notification.show("Failed to remove permissions: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                        3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error removing permissions: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void showAddManagerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        H2 title = new H2("Add New Manager");
        title.getStyle().set("color", "#2E7D32");

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
            Set<StoreManagerPermission> permissions = permissionsGroup.getSelectedItems();
            if (managerId == null || managerId <= 0 || permissions.isEmpty()) {
                errorMessage.setText("Please enter a valid Manager ID and select at least one permission.");
                errorMessage.getStyle().set("display", "block");
                return;
            }
            // Pass requesterId to addStoreManager
            addStoreManager(storeId, requesterId, managerId, new ArrayList<>(permissions), token);
            dialog.close();
        });
        confirmButton.getStyle().set("background-color", "#2E7D32").set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.getStyle().set("background-color", "#D32F2F").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(title, managerIdField, permissionsGroup, errorMessage, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> permissions, String token) {
        // Construct the URL to include requesterId
        String baseUrl = String.format(
                apiUrl + "store/addStoreManager/%d/%d/%d",
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
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();
            } else {
                Notification.show("Failed to add manager: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding manager: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void showAddOwnerDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        H2 title = new H2("Add New Owner");
        title.getStyle().set("color", "#2E7D32");

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
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(title, ownerIdField, errorMessage, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void addStoreOwner(int storeId, int requesterId, int ownerId, String token) {
        String url = String.format(apiUrl + "store/addStoreOwner/%d/%d/%d", storeId, requesterId, ownerId);
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
                // Refresh both initial buttons and roles display
                fetchUserPermissionsForStore(currentStoreId, currentUserDTO.getUserId(), currentToken);
                showStoreManagementSection();
            } else {
                Notification.show("Failed to add owner: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding owner: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void showAddProductDialog(int storeId, int requesterId, String token) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setWidth("400px");

        H2 title = new H2("Add New Product");
        title.getStyle().set("color", "#2E7D32");

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
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout layout = new VerticalLayout(title, productNameField, descriptionField, basePriceField, quantityField, categoryField, errorMessage, buttons);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void addProductToStore(int storeId, int requesterId, String productName, String description, double basePrice, int quantity, String category, String token) {
        String url = String.format(
            apiUrl + "store/addProductToStore/%d/%d?productName=%s&description=%s&basePrice=%f&quantity=%d&category=%s",
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
            } else {
                Notification.show("Failed to add product: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error adding product: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private StoreDTO getStoreDTO(int storeId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format(apiUrl + "store/viewStore/%d", storeId);

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


    private boolean isOwner(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String url = apiUrl + "/store/isStoreOwner/" + storeId + "/" + user.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Boolean>> apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<Boolean>>() {});
        Response<Boolean> response = apiResponse.getBody();
        if(response.isSuccess()){
            if(response.getData()){
                effectivePermissions.addAll(Set.of(StoreManagerPermission.values()));
                return true;
            }
            else{
                return false;
            }
        }
        else{
            Notification.show(response.getMessage());
            return false;
        }
    }

    private boolean isManager(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String url = apiUrl + "/store/isStoreManager/" + storeId + "/" + user.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<List<StoreManagerPermission>>> apiResponseManager = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<List<StoreManagerPermission>>>() {});
        Response<List<StoreManagerPermission>> resp = apiResponseManager.getBody();
        if(resp.isSuccess()){
            if(resp.getData() != null)
                effectivePermissions = new HashSet<>(resp.getData());
            return resp.getData() != null;
        }
        else{
            Notification.show(resp.getMessage());
            return false;
        }
    }


    private void showAddAuctionProductDialog(int storeId, int userId, String token) {
        StoreDTO store = getStoreDTO(storeId, token);
        Collection<StoreProductDTO> prods = store.getStoreProducts();

        // 1) Create the dialog
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        // 2) Create a ComboBox of StoreProductDTO
        ComboBox<StoreProductDTO> productCombo = new ComboBox<>("Select product");
        productCombo.setItems(prods);                        // set your DTOs
        productCombo.setItemLabelGenerator(StoreProductDTO::getName); 
        productCombo.setPlaceholder("— choose one —");
        productCombo.setWidthFull();

        // 3) Add it to the dialog
        dialog.add(productCombo);

        NumberField basePrice = new NumberField("Starting Price");
        dialog.add(basePrice);
        IntegerField timeToEnd = new IntegerField("Auction Time (in minutes)");
        dialog.add(timeToEnd);

        // 4) “Save” button that reads out the selected DTO’s id
        Button save = new Button("Save", e -> {
            StoreProductDTO selected = productCombo.getValue();
            Double price = basePrice.getValue();
            Integer minutes = timeToEnd.getValue();
            
            if (selected == null) {
                productCombo.focus();
                Notification.show("Please pick a product").setPosition(Notification.Position.MIDDLE);
                return;
            }
            if (price == null) {
                basePrice.focus();
                Notification.show("Please enter a starting price").setPosition(Notification.Position.MIDDLE);
                return;
            }
            if (minutes == null) {
                timeToEnd.focus();
                Notification.show("Please enter auction duration").setPosition(Notification.Position.MIDDLE);
                return;
            }
            int selectedProductId = selected.getProductId();
            createAuction(storeId, userId, selectedProductId, token, price, minutes);
            dialog.close();
        });
        save.getStyle().set("margin-top", "1em");
        dialog.add(save);
        dialog.open();
    }

    private void createAuction(int storeId, int userId, int productId, String token, double price, int minutes){
        String url = apiUrl + "store/addAuctionProductToStore/" + storeId + "/" + userId + "/" + productId + "?basePrice=" + price + "&MinutesToEnd=" + minutes;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResp = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Response<Void>>() {}
        );
        Response<Void> response = apiResp.getBody();
        if(response.isSuccess()){
            Notification.show("Auction Created Succefully");
        }
        else{
            Notification.show(response.getMessage());
        }
    }

    private void showMsgs(){
        Dialog dialog = new Dialog();
        Map<Integer, UserMsg> msgs = getMsgs();
        if(msgs.isEmpty()){
            dialog.add("No Messages");
        }
        else{
            for (Map.Entry<Integer, UserMsg> entry : msgs.entrySet()) {
                Integer user = entry.getValue().getUserId();
                String messageText = entry.getValue().getMsg();

                // ─── Build a “message card” ─────────────────────────────
                // Container for one message
                VerticalLayout messageCard = new VerticalLayout();
                messageCard.getStyle()
                        .set("border", "1px solid #ccc")
                        .set("border-radius", "4px")
                        .set("padding", "0.5em")
                        .set("margin-bottom", "0.5em")
                        .set("width", "100%");

                // user id
                H4 userTitle = new H4("From: " + String.valueOf(user));
                userTitle.getStyle().set("margin", "0");

                // The message body
                Span body = new Span(messageText);
                body.getStyle().set("white-space", "pre-wrap"); // preserve line breaks if any

                // Reply button stub
                Button replyBtn = new Button("Reply");
                replyBtn.addClickListener(evt -> {
                    dialog.close();
                    replyDialog(currentUserDTO.getUserId(), storeId, user);
                });

                // Assemble card
                messageCard.add(userTitle, body, replyBtn);

                // Add to the main container
                dialog.add(messageCard);
            }
        }
        dialog.open();
    }

    private void replyDialog(int managerId, int storeId, int userId){
        Dialog dialog = new Dialog();
        TextField textField = new TextField("Message");
        Button send = new Button("Send");
        send.addClickListener(e -> {if(!textField.isEmpty() && !textField.getValue().trim().isEmpty()){
                                     sendMsg(managerId, storeId, userId, textField.getValue());
                                     dialog.close();}
                                    });
        dialog.add(textField);
        dialog.add(send);
        dialog.open();
    }

    private void sendMsg(int managerId, int storeId, int userId, String msg){
        String url = apiUrl + "store/sendMessageToUser/" + managerId + "/" + storeId +"/" + userId;
        Request<String> req = new Request<>(currentToken, msg);
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Request<String>> entity = new HttpEntity<>(req, header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.POST, 
            entity, 
            new ParameterizedTypeReference<Response<Void>>() {});
        Response<Void> response = apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Message Sent Successfully");
        }
        else{
            Notification.show(response.getMessage());
        }
    }

    private Map<Integer, UserMsg> getMsgs(){
        String url = apiUrl + "store/getMessagesFromUsers/" +storeId + "/" + currentUserDTO.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Map<Integer,UserMsg>>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            new ParameterizedTypeReference<Response<Map<Integer, UserMsg>>>() {});
        Response<Map<Integer, UserMsg>> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData();
        }
        else{
            Notification.show(response.getMessage());
            return new HashMap<>();
        }
    }

    private void viewPurchases(){
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        List<OrderDTO> orders = getPurchases();
        if (orders == null || orders.isEmpty()) {
            content.add(new H1("No Orders"));
        } else {
            Accordion accordion = new Accordion();
            for (OrderDTO order : orders) {
                // Panel title
                String title = String.format("Order #%d - %s", order.getOrderId(), order.getOrderState());
                // Details layout inside panel
                VerticalLayout detailsLayout = new VerticalLayout();
                detailsLayout.setPadding(false);
                detailsLayout.setSpacing(true);

                // Order metadata
                detailsLayout.add(new H3("Order Details"));
                Div meta = new Div();
                meta.getStyle().set("display", "grid");
                meta.getStyle().set("grid-template-columns", "1fr 1fr");
                meta.getStyle().set("gap", "10px");
                meta.add(new Div(new H3("Address: "), new Div(order.getAddress())));
                meta.add(new Div(new H3("Payment: "), new Div(order.getPaymentMethod())));
                meta.add(new Div(new H3("Store ID: "), new Div(String.valueOf(order.getStoreId()))));
                meta.add(new Div(new H3("User ID: "), new Div(String.valueOf(order.getUserId()))));
                double totalPrice = order.getTotalPrice();
                String formattedPrice = String.format("%.2f", totalPrice);
                meta.add(new Div(new H3("Total Price: "), new Div(formattedPrice)));
                detailsLayout.add(meta);

                // Products grid
                detailsLayout.add(new H3("Products"));
                Grid<OrderedProductDTO> grid = new Grid<>(OrderedProductDTO.class, false);
                grid.addColumn(OrderedProductDTO::getName).setHeader("Name");
                grid.setItems(order.getProducts());
                grid.setHeight("200px");               // or use setSizeFull() only
                detailsLayout.add(grid);
                AccordionPanel panel = accordion.add(title, detailsLayout);
                panel.setHeight("300px");
            }
            content.add(accordion);
        }

        dialog.add(content);
        dialog.open();
    }

    
    private List<OrderDTO> getPurchases(){
        String url = apiUrl + "store/getAllStoreOrders/" +storeId + "/" + currentUserDTO.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<List<OrderDTO>>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            new ParameterizedTypeReference<Response<List<OrderDTO>>>() {});
        Response<List<OrderDTO>> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData();
        }
        else{
            Notification.show(response.getMessage());
            return new ArrayList<>();
        }
    }


    private void removeProductDialog(){
        StoreDTO store = getStoreDTO(storeId, currentToken);
        Collection<StoreProductDTO> prods = store.getStoreProducts();

        // 1) Create the dialog
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        // 2) Create a ComboBox of StoreProductDTO
        ComboBox<StoreProductDTO> productCombo = new ComboBox<>("Select product");
        productCombo.setItems(prods);                        // set your DTOs
        productCombo.setItemLabelGenerator(StoreProductDTO::getName); 
        productCombo.setPlaceholder("— choose one —");
        productCombo.setWidthFull();

        // 3) Add it to the dialog
        dialog.add(productCombo);
        
        Button removeButton = new Button("Remove");
        removeButton.addClickListener(e -> {
            if(productCombo.getValue() != null){
                dialog.close();
                confirmRemoveDialog(productCombo.getValue());
            }
        });
        dialog.add(removeButton);
        dialog.open();
    }

    private void confirmRemoveDialog(StoreProductDTO product){
        Dialog dialog = new Dialog();
        dialog.add(new H3("This can not be undone and will also remove all auction listings\n of the product if there are any"));
        Button confirm = new Button("Confirm Removal");
        confirm.addClickListener(e -> {removeProduct(product); dialog.close();});
        dialog.add(confirm);
        dialog.open();
    }

    private void removeProduct(StoreProductDTO product){
        String url = apiUrl + "store/removeProductFromStore/" + currentStoreId + "/" + currentUserDTO.getUserId() + "/" + product.getProductId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.DELETE, 
            entity, 
            new ParameterizedTypeReference<Response<Void>>() {});
        Response<Void> response = apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Removed Successfully");
        }
        else{
            Notification.show(response.getMessage());
        }

    }

    private void editProductDialog() {
        StoreDTO store = getStoreDTO(storeId, currentToken);
        Collection<StoreProductDTO> prods = store.getStoreProducts();

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        ComboBox<StoreProductDTO> productCombo = new ComboBox<>("Select product");
        productCombo.setItems(prods);
        productCombo.setItemLabelGenerator(StoreProductDTO::getName);
        productCombo.setPlaceholder("— choose one —");
        productCombo.setWidthFull();

        // Fields for details
        Label nameLabel = new Label();
        Label categoryLabel = new Label();
        Label ratingLabel = new Label();

        NumberField basePriceField = new NumberField("Base Price");
        IntegerField quantityField = new IntegerField("Quantity");
        
        basePriceField.setWidthFull();
        quantityField.setWidthFull();

        // Layout to hold the fields
        FormLayout formLayout = new FormLayout();
        formLayout.setVisible(false); // Initially hidden

        formLayout.addFormItem(nameLabel, "Name");
        formLayout.addFormItem(categoryLabel, "Category");
        formLayout.addFormItem(ratingLabel, "Average Rating");
        formLayout.add(basePriceField, quantityField);
        Button confirmButton = new Button("Update");
        confirmButton.addClickListener(e -> {
            StoreProductDTO selected = productCombo.getValue();
            if(selected != null && basePriceField.getValue() != null && quantityField.getValue() != null
                && basePriceField.getValue() > 0 && quantityField.getValue() > 0){
                    dialog.close();
                    editProduct(selected, basePriceField.getValue(), quantityField.getValue());
            }
        });
        formLayout.add(confirmButton);

        productCombo.addValueChangeListener(event -> {
            StoreProductDTO selected = event.getValue();
            if (selected != null) {
                nameLabel.setText(selected.getName());
                categoryLabel.setText(selected.getCategory().toString());
                ratingLabel.setText(String.valueOf(selected.getAverageRating()));
                basePriceField.setValue(selected.getBasePrice());
                quantityField.setValue(selected.getQuantity());
                formLayout.setVisible(true);
            } else {
                formLayout.setVisible(false);
            }
        });

        dialog.add(productCombo, formLayout);
        dialog.open();
    }

    private void editProduct(StoreProductDTO product, Double basePrice, Integer quantity){
        String url = apiUrl + "store/updateProductInStore/" + currentStoreId + "/" + currentUserDTO.getUserId() +"/" + product.getProductId() + "?basePrice=" + basePrice + "&quantity=" + quantity;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.POST, 
            entity, 
            new ParameterizedTypeReference<Response<Void>>() {});
        Response<Void> response = apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Product Edited Successfully");
        }
        else{
            Notification.show(response.getMessage());
        }
    }

    private void closeStoreDialog(){
        Dialog dialog = new Dialog();
        dialog.add(new H2("Confirm Closing Store"));
        dialog.add(new Button("Confirm", e -> {dialog.close(); closeStore();}));
        dialog.open();
    }

    private void openStoreDialog() {
        Dialog dialog = new Dialog();
        dialog.add(new H2("Confirm Opening Store"));
        dialog.add(new Button("Confirm", e -> {dialog.close(); openStore(); }));
        dialog.open();
    }

    private void closeStore(){
        String url = apiUrl + "store/closeStoreByFounder/" + storeId + "/" + currentUserDTO.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<String>> apiResp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<String>>() {
                });
        Response<String> response = apiResp.getBody();
        if (response.isSuccess()) {
            Notification.show("Store Closed Successfully");
        } else {
            Notification.show(response.getMessage());
        }
    }

    private void openStore() {
        String url = apiUrl + "store/openStore/" + storeId + "/" + currentUserDTO.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", currentToken);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResp.getBody();
        if (response.isSuccess()) {
            Notification.show("Store Opened Successfully");
        } else {
            Notification.show(response.getMessage());
        }
    }


}