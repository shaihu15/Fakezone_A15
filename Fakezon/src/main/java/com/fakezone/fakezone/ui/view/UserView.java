package com.fakezone.fakezone.ui.view;


import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import DomainLayer.Enums.RoleName;
import DomainLayer.Model.helpers.StoreMsg;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout implements BeforeEnterObserver {
    private final String backendUrl;
    private final String websiteUrl;
    private final RestTemplate restTemplate;

    // Declare card layouts as instance variables
    private VerticalLayout ownedStoresCard;
    private VerticalLayout managedStoresCard;
    private VerticalLayout pendingAssignmentsCard;
    private VerticalLayout messagesFromStoreCard;
    private VerticalLayout offerMessagesCard;
    private Button openStoreButton; // Also declare button as instance variable

    private static final Set<String> OWNERSHIP_ROLES = Set.of(
        RoleName.STORE_FOUNDER.name(),
        RoleName.STORE_OWNER.name()
    );
    private static final Set<String> MANAGEMENT_ROLES = Set.of(
        RoleName.STORE_MANAGER.name()
    );

    public UserView(@Value("${api.url}") String apiUrl, @Value("${website.url}") String webUrl) {
        this.backendUrl = apiUrl;
        this.websiteUrl = webUrl;
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());

        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        // Initialize cards and button here, in the constructor
        // This ensures they are created only once when the view is first initialized
        ownedStoresCard = createCardLayout("My Stores");
        managedStoresCard = createCardLayout("Stores I Manage");
        pendingAssignmentsCard = createCardLayout("Pending Assignments");
        messagesFromStoreCard = createCardLayout("Messages From Stores");
        offerMessagesCard = createCardLayout("Auction Messages");
        openStoreButton = new Button("Open a Store"); // Listener will be set in beforeEnter

        // Add them to the view initially. Their content will be updated in beforeEnter.
        add(openStoreButton, ownedStoresCard, managedStoresCard, pendingAssignmentsCard, messagesFromStoreCard, offerMessagesCard);
    }

    // Helper method to create a card layout with common styling
    private VerticalLayout createCardLayout(String title) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle().set("border", "1px solid #ccc").set("border-radius", "8px");
        card.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        card.add(new H2(title));
        return card;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        UserDTO userDTO = session != null ? (UserDTO) session.getAttribute("userDTO") : null;

        if (token == null || userDTO == null) {
            event.rerouteTo("");
            Notification.show("Please log in to view this page.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (isGuestToken(session)) {
            event.rerouteTo("");
            Notification.show("Please log in as a registered user to view this page.", 3000, Notification.Position.MIDDLE);
            return;
        }

        int userId = userDTO.getUserId();
        String tokenValue = token; // Rename to avoid confusion with parameter

        // Set the listener for the button here, or ensure it only happens once
        // If it's always the same, you can set it in the constructor
        // If userId or token change, then set it here, but ensure previous listeners are removed if re-added
        openStoreButton.addClickListener(e -> openStoreDialog(userId, tokenValue));


        // Clear existing content before adding new data
        updateOwnedStores(userId, tokenValue);
        updateManagedStores(userId, tokenValue);
        refreshPendingAssignments(userId, tokenValue); // This already clears its content
        updateMsgFromStores(userId, tokenValue);
        updateOfferMsgs(userId, tokenValue);
    }

    private void updateOwnedStores(int userId, String token) {
        ownedStoresCard.removeAll(); // Clear existing content (except the H2 title)
        ownedStoresCard.add(new H2("My Stores")); // Re-add the title

        Map<Integer, String> userRoles = getUserRoles(token, userId);
        List<Component> ownedStores = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : userRoles.entrySet()) {
            int storeId = entry.getKey();
            String roleType = entry.getValue();

            StoreDTO store = getStoreDTO(storeId, token);
            if (store != null) {
                String effectiveRole = (roleType != null) ? roleType : RoleName.UNASSIGNED.name();
                if (OWNERSHIP_ROLES.contains(effectiveRole)) {
                    ownedStores.add(createStoreCard(store, effectiveRole));
                }
            }
        }

        if (!ownedStores.isEmpty()) {
            ownedStores.forEach(ownedStoresCard::add);
        } else {
            ownedStoresCard.add(new Span("You do not own any stores."));
        }
    }

    private void updateManagedStores(int userId, String token) {
        managedStoresCard.removeAll(); // Clear existing content (except the H2 title)
        managedStoresCard.add(new H2("Stores I Manage")); // Re-add the title

        Map<Integer, String> userRoles = getUserRoles(token, userId);
        List<Component> managedStores = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : userRoles.entrySet()) {
            int storeId = entry.getKey();
            String roleType = entry.getValue();

            StoreDTO store = getStoreDTO(storeId, token);
            if (store != null) {
                String effectiveRole = (roleType != null) ? roleType : RoleName.UNASSIGNED.name();
                if (MANAGEMENT_ROLES.contains(effectiveRole)) {
                    managedStores.add(createStoreCard(store, effectiveRole));
                }
            }
        }

        if (!managedStores.isEmpty()) {
            managedStores.forEach(managedStoresCard::add);
        } else {
            managedStoresCard.add(new Span("You do not manage any stores."));
        }
    }


    private void acceptAssignment(int storeId, int userId, String token, VerticalLayout assignmentCard) {
        String url = String.format(backendUrl + "store/acceptAssignment/%d/%d", storeId, userId);
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
                Notification.show("Assignment accepted successfully.", 3000, Notification.Position.MIDDLE);
                // Instead of reloading the page, update the specific sections
                refreshPendingAssignments(userId, token);
                updateManagedStores(userId, token); // The newly accepted store might be a managed store
                updateOwnedStores(userId, token); // Or an owned store
            } else {
                Notification.show("Failed to accept assignment: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"), 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error accepting assignment: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void declineAssignment(int storeId, int userId, String token, VerticalLayout assignmentCard) {
        String url = String.format(backendUrl + "store/declineAssignment/%d/%d", storeId, userId);
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
                Notification.show("Assignment declined successfully.", 3000, Notification.Position.MIDDLE);
                refreshPendingAssignments(userId, token); // Only refresh pending assignments
            } else {
                Notification.show("Failed to decline assignment: " + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"), 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Error declining assignment: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void refreshPendingAssignments(int userId, String token) {
        pendingAssignmentsCard.removeAll();
        pendingAssignmentsCard.add(new H2("Pending Assignments")); // Re-add the title

        String assignmentsUrl = String.format(backendUrl + "user/getAssignmentMessages/%d", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Map<Integer, StoreMsg>>> response =
                restTemplate.exchange(
                    assignmentsUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                Map<Integer, StoreMsg> assignments = response.getBody().getData();
                if (assignments != null && !assignments.isEmpty()) {
                    for (Map.Entry<Integer, StoreMsg> entry : assignments.entrySet()) {
                        int storeId = entry.getValue().getStoreId();
                        String message = entry.getValue().getMessage();
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
        String url = backendUrl + "store/addStore/" + userId + "/" + storeName;
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
                // Instead of reloading the page, update the relevant section directly
                updateOwnedStores(userId, token); // The new store is an owned store
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
        String url = backendUrl + "user/isGuestToken";
        try {
            ResponseEntity<Response> apiResponse = restTemplate.postForEntity(url, token, Response.class);
            Response<Boolean> response = (Response<Boolean>) apiResponse.getBody();
            return response.isSuccess() && response.getData();
        } catch (Exception e) {
            Notification.show("Error checking token: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            return true;
        }
    }

    private Map<Integer, String> getUserRoles(String token, int userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format(backendUrl + "user/userRoles/%d", userId);

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

        String url = String.format(backendUrl + "store/viewStore/%d", storeId);

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
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
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

        RouterLink link = new RouterLink(store.getName(), StoreManageView.class, new RouteParameters("storeId", String.valueOf(store.getStoreId())));        
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

    private void updateMsgFromStores(int userId, String token){
        messagesFromStoreCard.removeAll();
        messagesFromStoreCard.add(new H2("Messages From Stores"));
        String url = backendUrl + "user/getMessagesFromStore/" + userId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Map<Integer, StoreMsg>>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            new ParameterizedTypeReference<Response<Map<Integer, StoreMsg>>>() {});
        Response<Map<Integer, StoreMsg>> response = apiResponse.getBody();
        if(response.isSuccess()){
            if(response.getData() == null || response.getData().isEmpty()){
                messagesFromStoreCard.add(new Span("No Messages from Stores"));
            }
            else{
                Map<Integer, StoreMsg> msgs  = response.getData();
                for (Map.Entry<Integer, StoreMsg> entry : msgs.entrySet()) {
                    Integer storeId = entry.getValue().getStoreId();
                    String messageText = entry.getValue().getMessage();
                    StoreMsg msgObj = entry.getValue();
                    // Fetch store info
                    StoreDTO store = getStoreDTO(storeId, token);
                    String storeName = store.getName();

                    // ─── Build a “message card” ─────────────────────────────
                    // Container for one message
                    VerticalLayout messageCard = new VerticalLayout();
                    messageCard.getStyle()
                            .set("border", "1px solid #ccc")
                            .set("border-radius", "4px")
                            .set("padding", "0.5em")
                            .set("margin-bottom", "0.5em")
                            .set("width", "100%");

                    // Store name (bold)
                    H4 nameHeader = new H4(storeName);
                    nameHeader.getStyle().set("margin", "0");

                    // The message body
                    Span body = new Span(messageText);
                    body.getStyle().set("white-space", "pre-wrap"); // preserve line breaks if any

                    // Reply button 
                    Button replyBtn = new Button("Reply");
                    replyBtn.addClickListener(evt -> {
                        replyDialog(userId, storeId, token);
                    });

                    // Bin Button
                    Button binButton = new Button(new Icon(VaadinIcon.TRASH));
                    binButton.addClickListener(e -> {removeMessage(userId, entry.getKey(), token); updateMsgFromStores(userId, token);});
                    // Assemble card
                    messageCard.add(nameHeader, body, replyBtn, binButton);
                    
                    if (msgObj.isCounterOffer()) {
                        Button accCounter = new Button("Accept");
                        accCounter.addClickListener(e -> acceptCounterOffer(userId, msgObj.getStoreId() ,msgObj.getProductId(), token, entry.getKey()));
                        Button decCounter = new Button("Decline");
                        decCounter.addClickListener(e -> declineCounterOffer(userId, msgObj.getStoreId(), msgObj.getProductId(), token, entry.getKey()));
                        messageCard.add(accCounter, decCounter);
                    }
                    // Add to the main container
                    messagesFromStoreCard.add(messageCard);
                }
            }
        }
        else{
            Notification.show(response.getMessage());
        }
    }

    private void replyDialog(int userId, int storeId, String token){
        Dialog dialog = new Dialog();
        TextField textField = new TextField("Message");
        Button send = new Button("Send");
        send.addClickListener(e -> {if(!textField.isEmpty() && !textField.getValue().trim().isEmpty()){
                                     sendMsg(userId, storeId, textField.getValue(), token);
                                     dialog.close();}
                                    });
        dialog.add(textField);
        dialog.add(send);
        dialog.open();
    }

    private void sendMsg(int userId, int storeId, String msg, String token){
        String url = backendUrl + "user/sendMessageToStore/" + userId + "/" + storeId;
        Request<String> req = new Request<>(token, msg);
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
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

    private void updateOfferMsgs(int userId, String token){
        offerMessagesCard.removeAll();
        offerMessagesCard.add(new H2("Offer Messages"));
        String url = backendUrl + "user/getOfferMessages/" + userId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Map<Integer, StoreMsg>>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            new ParameterizedTypeReference<Response<Map<Integer, StoreMsg>>>() {});
        Response<Map<Integer, StoreMsg>> response = apiResponse.getBody();
        if(response.isSuccess()){
            if(response.getData() == null || response.getData().isEmpty()){
                offerMessagesCard.add(new Span("No Offer Messages"));
            }
            else{
                Map<Integer, StoreMsg> msgs  = response.getData();
                for (Map.Entry<Integer, StoreMsg> entry : msgs.entrySet()) {
                    Integer storeId = entry.getValue().getStoreId();
                    String messageText = entry.getValue().getMessage();

                    // Fetch store info
                    StoreDTO store = getStoreDTO(storeId, token);
                    String storeName = store.getName();

                    // ─── Build a “message card” ─────────────────────────────
                    // Container for one message
                    VerticalLayout messageCard = new VerticalLayout();
                    messageCard.getStyle()
                            .set("border", "1px solid #ccc")
                            .set("border-radius", "4px")
                            .set("padding", "0.5em")
                            .set("margin-bottom", "0.5em")
                            .set("width", "100%");

                    // Store name (bold)
                    H4 nameHeader = new H4(storeName);
                    nameHeader.getStyle().set("margin", "0");

                    // The message body
                    Span body = new Span(messageText);
                    body.getStyle().set("white-space", "pre-wrap"); // preserve line breaks if any
                    
                    int productId = entry.getValue().getProductId();
                    // Reply button stub
                    Button accButton = new Button("Accept");
                    accButton.addClickListener(evt -> {
                        acceptOffer(storeId, userId, productId, token, entry.getKey(), entry.getValue().getOfferedBy()); //user id = owner (this user), getOfferedBy = user that placed the offer
                    });

                    Button decButton = new Button("Decline");
                    decButton.addClickListener(evt -> {
                        declineOffer(storeId, userId, productId, token, entry.getKey(),  entry.getValue().getOfferedBy()); //user id = owner (this user), getOfferedBy = user that placed the offer
                    });

                    Button counterBtn = new Button("Counter Offer");
                    counterBtn.addClickListener(evt -> {
                        counterOfferDialog(storeId, userId, productId, token, entry.getKey(),entry.getValue().getOfferedBy()); // user id = owner (this user), getOfferedBy = user that placed the offer
                    });

                    // Assemble card
                    messageCard.add(nameHeader, body, accButton, decButton, counterBtn);

                    // Add to the main container
                    offerMessagesCard.add(messageCard);
                }
            }
        }
        else{
            Notification.show(response.getMessage());
        }
    }

    private void removeMessage(int userId, int msgId, String token){
        String url = backendUrl + "user/removeUserMessageById/" + userId + "/" + msgId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResponse.getBody();
        if (response.isSuccess()) {
            Notification.show("Message Removed Successfully");
        } else {
            Notification.show(response.getMessage());
        }
    }


    private void acceptOffer(int storeId, int ownerId, int productId, String token, int msgId, int userId){
        String url = backendUrl + "store/acceptOfferOnStoreProduct/" + storeId + "/" + ownerId + "/" + userId + "/" + productId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResponse.getBody();
        if (response.isSuccess()) {
            Notification.show("Offer accepted Successfully");
            removeMessage(ownerId, msgId, token);
            updateOfferMsgs(ownerId, token);
        } else {
            Notification.show(response.getMessage());
        }
    }

    private void declineOffer(int storeId, int ownerId, int productId, String token, int msgId, int userId) {
        String url = backendUrl + "store/declineOfferOnStoreProduct/" + storeId + "/" + ownerId + "/" + userId + "/" + productId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResponse.getBody();
        if (response.isSuccess()) {
            Notification.show("Offer Declined Successfully");
            removeMessage(ownerId, msgId, token);
            updateOfferMsgs(ownerId, token);
        } else {
            Notification.show(response.getMessage());
        }
    }

    private void counterOfferDialog(int storeId, int ownerId, int productId, String token, int msgId, int userId){
        Dialog dialog = new Dialog();
        NumberField offer = new NumberField("Enter Your Counter Offer");
        dialog.add(offer);
        Button send = new Button("Send");
        send.addClickListener(e -> {if(offer.getValue() != null && offer.getValue() > 0){
            dialog.close(); sendCounterOffer(storeId, ownerId, productId, token, msgId, userId, offer.getValue());
        } });
        dialog.add(send);
        dialog.open();
    }

    private void sendCounterOffer(int storeId, int ownerId, int productId, String token, int msgId, int userId, double offerAmount){
        String url = backendUrl + "store/counterOffer/" + storeId + "/" + ownerId + "/" + userId + "/" + productId + "?offerAmount=" + offerAmount;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResp.getBody();
        if (response.isSuccess()) {
            Notification.show("Counter Offer Submitted Successfully");
            removeMessage(ownerId, msgId, token);
            updateOfferMsgs(ownerId, token);
        } else {
            Notification.show(response.getMessage());
        }
    }

    private void acceptCounterOffer(int userId, int storeId , int productId, String token, int msgId){
        String url = backendUrl + "store/acceptCounterOffer/" + storeId + "/" + userId + "/" + productId ;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResp.getBody();
        if (response.isSuccess()) {
            Notification.show("Counter Offer Accepted Successfully");
            removeMessage(userId, msgId, token);
            updateMsgFromStores(userId, token);
        } else {
            Notification.show(response.getMessage());
        }
    }

    private void declineCounterOffer(int userId, int storeId, int productId, String token, int msgId) {
        String url = backendUrl + "store/declineCounterOffer/" + storeId + "/" + userId + "/" + productId;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {
                });
        Response<Void> response = apiResp.getBody();
        if (response.isSuccess()) {
            Notification.show("Counter Offer Declined Successfully");
            removeMessage(userId, msgId, token);
            updateMsgFromStores(userId, token);
        } else {
            Notification.show(response.getMessage());
        }
    }
}