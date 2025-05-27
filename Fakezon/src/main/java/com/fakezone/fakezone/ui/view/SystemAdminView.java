// package com.fakezone.fakezone.ui.view;
// import com.vaadin.flow.component.UI;
// import com.vaadin.flow.component.button.Button;
// import com.vaadin.flow.component.datepicker.DatePicker;
// import com.vaadin.flow.component.dialog.Dialog;
// import com.vaadin.flow.component.grid.Grid;
// import com.vaadin.flow.component.html.H2;
// import com.vaadin.flow.component.html.Paragraph;
// import com.vaadin.flow.component.notification.Notification;
// import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
// import com.vaadin.flow.component.orderedlayout.VerticalLayout;
// import com.vaadin.flow.component.textfield.TextField;
// import com.vaadin.flow.data.value.ValueChangeMode;
// import com.vaadin.flow.router.BeforeEnterEvent;
// import com.vaadin.flow.router.BeforeEnterObserver;
// import com.vaadin.flow.router.PageTitle;
// import com.vaadin.flow.router.Route;
// import com.vaadin.flow.server.VaadinRequest;
// import jakarta.annotation.security.RolesAllowed;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpSession;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.ParameterizedTypeReference;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.client.DefaultResponseErrorHandler;
// import org.springframework.web.client.RestTemplate;

// import ApplicationLayer.Response;
// import ApplicationLayer.DTO.UserDTO;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Locale;
// import java.util.Optional;

// @Route(value = "admin/users", layout = MainLayout.class)
// @PageTitle("System Admin | User Management")
// @RolesAllowed("ADMIN") // Ensure only ADMIN roles can access this view
// public class SystemAdminView extends VerticalLayout implements BeforeEnterObserver {

//     private final String backendUrl;
//     private final RestTemplate restTemplate;

//     private Grid<UserDTO> userGrid = new Grid<>(UserDTO.class, false);
//     private TextField searchField = new TextField();
//     private Button manageUserSuspensionButton = new Button("Manage Suspension");
//     private Button viewSuspendedUsersButton = new Button("View Suspended Users");
//     private Button refreshAllUsersButton = new Button("Refresh All Users");
//     private Button cleanupSuspensionsButton = new Button("Cleanup Expired Suspensions");

//     public SystemAdminView(@Value("${api.url}") String apiUrl) {
//         this.backendUrl = apiUrl;
//         this.restTemplate = new RestTemplate();
//         this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler()); // Default error handling

//         addClassName("system-admin-view");
//         setSizeFull();
//         setPadding(true);
//         setSpacing(true);
//         setDefaultHorizontalComponentAlignment(Alignment.CENTER);

//         // Configure and add components
//         configureGrid();
//         configureSearch();
//         configureButtons();

//         HorizontalLayout toolbar = new HorizontalLayout(
//                 searchField, refreshAllUsersButton, viewSuspendedUsersButton,
//                 manageUserSuspensionButton, cleanupSuspensionsButton
//         );
//         toolbar.setAlignItems(Alignment.CENTER);
//         toolbar.setWidthFull();

//         add(new H2("System Admin - User Management"), toolbar, userGrid);
//     }

//     /**
//      * Configures the user grid columns and selection listener.
//      */
//     private void configureGrid() {
//         userGrid.addColumn(UserDTO::getUserId).setHeader("User ID").setSortable(true);
//         userGrid.addColumn(UserDTO::getUserEmail).setHeader("Email").setSortable(true);
//         userGrid.addColumn(UserDTO::getUserAge).setHeader("Age").setSortable(true);
//         // Add more columns as needed from UserDTO

//         userGrid.getColumns().forEach(col -> col.setAutoWidth(true));
//         userGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

//         userGrid.asSingleSelect().addValueChangeListener(event -> {
//             // Enable the suspension button only if a user is selected
//             boolean hasSelection = event.getValue() != null;
//             manageUserSuspensionButton.setEnabled(hasSelection);
//         });

//         manageUserSuspensionButton.setEnabled(false); // Disabled by default until a user is selected
//     }

//     /**
//      * Configures the search field for filtering users.
//      */
//     private void configureSearch() {
//         searchField.setPlaceholder("Search by Email or ID...");
//         searchField.setValueChangeMode(ValueChangeMode.LAZY); // Updates on a slight delay after typing
//         searchField.addValueChangeListener(e -> filterUsers(e.getValue()));
//     }

//     /**
//      * Configures click listeners for all action buttons.
//      */
//     private void configureButtons() {
//         refreshAllUsersButton.addClickListener(e -> fetchAllUsers());
//         viewSuspendedUsersButton.addClickListener(e -> fetchSuspendedUsers());
//         manageUserSuspensionButton.addClickListener(e -> {
//             UserDTO selectedUser = userGrid.asSingleSelect().getValue();
//             if (selectedUser != null) {
//                 showManageSuspensionDialog(selectedUser);
//             } else {
//                 Notification.show("Please select a user to manage suspension.", 3000, Notification.Position.MIDDLE);
//             }
//         });
//         cleanupSuspensionsButton.addClickListener(e -> cleanupExpiredSuspensions());
//     }

//     // --- Session and Token Helper Methods ---

//     private HttpSession getSession() {
//         HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
//         return httpRequest.getSession(false); // false means don't create if it doesn't exist
//     }

//     private Optional<String> getTokenFromSession() {
//         HttpSession session = getSession();
//         return (session != null) ? Optional.ofNullable((String) session.getAttribute("token")) : Optional.empty();
//     }

//     private Optional<UserDTO> getUserDTOFromSession() {
//         HttpSession session = getSession();
//         return (session != null) ? Optional.ofNullable((UserDTO) session.getAttribute("userDTO")) : Optional.empty();
//     }

//     /**
//      * Checks if the current session token belongs to a guest user.
//      * This method is crucial for authentication and authorization.
//      *
//      * @param session The current HttpSession.
//      * @return true if the token is a guest token or if there's an error, false otherwise.
//      */
//     private boolean isGuestToken(HttpSession session) {
//         String token = (String) session.getAttribute("token");
//         if (token == null) {
//             return true; // No token means guest or unauthenticated
//         }

//         // Backend URL for checking if a token is a guest token
//         // IMPORTANT: Ensure this URL matches your backend endpoint precisely.
//         // Based on UserView and previous discussions, "/api/" might not be needed here.
//         String url = backendUrl + "user/isGuestToken";

//         HttpHeaders headers = new HttpHeaders();
//         // Assuming the backend expects the token as a raw string in the request body.
//         // If it expects JSON, you'll need to send a DTO. If it expects a header, set it in headers.
//         headers.setContentType(MediaType.TEXT_PLAIN); // Indicate that the request body is plain text
//         HttpEntity<String> entity = new HttpEntity<>(token, headers); // Wrap the token in an HttpEntity

//         try {
//             // Use restTemplate.exchange for better handling of generic types (like Response<Boolean>)
//             ResponseEntity<Response<Boolean>> apiResponse = restTemplate.exchange(
//                 url,
//                 HttpMethod.POST,
//                 entity,
//                 new ParameterizedTypeReference<Response<Boolean>>() {} // Specify the generic type
//             );

//             Response<Boolean> responseBody = apiResponse.getBody();
//             // Check if the response is successful and the data indicates it's a guest token
//             return responseBody != null && responseBody.isSuccess() && responseBody.getData();
//         } catch (Exception e) {
//             // Log the exception for debugging purposes
//             e.printStackTrace();
//             Notification.show("Error checking token: " + e.getMessage() + ". Check backend status and API URL.", 5000, Notification.Position.MIDDLE);
//             // If an error occurs during the check, assume it's a guest token or an invalid state, forcing re-login
//             return true;
//         }
//     }

//     /**
//      * Vaadin lifecycle method called before entering the view.
//      * Handles authentication and initial data loading.
//      */
//     @Override
//     public void beforeEnter(BeforeEnterEvent event) {
//         HttpSession session = getSession();
//         String token = getTokenFromSession().orElse(null);
//         UserDTO userDTO = getUserDTOFromSession().orElse(null);

//         // 1. Check for basic authentication (token and UserDTO in session)
//         if (token == null || userDTO == null) {
//             event.rerouteTo("login");
//             Notification.show("Please log in to view this page.", 3000, Notification.Position.MIDDLE);
//             return;
//         }

//         // 2. Check if the token belongs to a guest user (admins should not be guests)
//         if (isGuestToken(session)) {
//             event.rerouteTo("login");
//             Notification.show("Please log in as a registered user (non-guest) to view this page.", 3000, Notification.Position.MIDDLE);
//             return;
//         }

//         // If authenticated and not a guest, fetch all users for the grid
//         fetchAllUsers();
//     }

//     // --- Data Fetching Methods ---

//     /**
//      * Fetches all users from the backend and updates the grid.
//      */
//     private void fetchAllUsers() {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }

//         // Endpoint: /admin/users/all/{requesterId}
//         String url = backendUrl + "admin/users/all/" + currentUser.getUserId();

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token); // Send token in Authorization header
//         HttpEntity<Void> entity = new HttpEntity<>(headers);

//         try {
//             ResponseEntity<Response<List<UserDTO>>> apiResponse = restTemplate.exchange(
//                     url,
//                     HttpMethod.GET,
//                     entity,
//                     new ParameterizedTypeReference<Response<List<UserDTO>>>() {}
//             );

//             Response<List<UserDTO>> response = apiResponse.getBody();
//             if (response != null && response.isSuccess() && response.getData() != null) {
//                 userGrid.setItems(response.getData());
//                 Notification.show("All users list updated.", 1500, Notification.Position.BOTTOM_END);
//                 userGrid.asSingleSelect().clear(); // Clear selection after refresh
//             } else {
//                 Notification.show("Failed to fetch all users: " + (response != null ? response.getMessage() : "Unknown error"), 3000, Notification.Position.MIDDLE);
//                 userGrid.setItems(); // Clear grid on error
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             Notification.show("Error fetching all users: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
//             userGrid.setItems(); // Clear grid on error
//         }
//     }

//     /**
//      * Fetches only suspended users from the backend and updates the grid.
//      */
//     private void fetchSuspendedUsers() {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }

//         // Endpoint: /getAllSuspendedUsers/{requesterId}
//         String url = backendUrl + "getAllSuspendedUsers/" + currentUser.getUserId();

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token);
//         HttpEntity<Void> entity = new HttpEntity<>(headers);

//         try {
//             ResponseEntity<Response<List<UserDTO>>> apiResponse = restTemplate.exchange(
//                     url,
//                     HttpMethod.GET,
//                     entity,
//                     new ParameterizedTypeReference<Response<List<UserDTO>>>() {}
//             );

//             Response<List<UserDTO>> response = apiResponse.getBody();
//             if (response != null && response.isSuccess() && response.getData() != null) {
//                 userGrid.setItems(response.getData());
//                 Notification.show("Suspended users list updated.", 1500, Notification.Position.BOTTOM_END);
//                 userGrid.asSingleSelect().clear();
//             } else {
//                 Notification.show("Failed to fetch suspended users: " + (response != null ? response.getMessage() : "Unknown error"), 3000, Notification.Position.MIDDLE);
//                 userGrid.setItems();
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             Notification.show("Error fetching suspended users: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
//             userGrid.setItems();
//         }
//     }

//     /**
//      * Filters users currently displayed in the grid based on a search term.
//      * This performs client-side filtering on the current grid items.
//      * For large datasets, server-side filtering would be more efficient.
//      */
//     private void filterUsers(String searchTerm) {
//         if (searchTerm == null || searchTerm.trim().isEmpty()) {
//             fetchAllUsers(); // If search term is empty, reload all users
//             return;
//         }

//         String lowerCaseSearchTerm = searchTerm.toLowerCase(Locale.ROOT);
//         userGrid.setItems(
//                 userGrid.getListDataView().getItems()
//                         .filter(user ->
//                                 String.valueOf(user.getUserId()).contains(lowerCaseSearchTerm) ||
//                                 user.getUserEmail().toLowerCase(Locale.ROOT).contains(lowerCaseSearchTerm)
//                         )
//                         .toList()
//         );
//     }

//     // --- Suspension Management Dialog and Actions ---

//     /**
//      * Displays a dialog to manage suspension for a selected user.
//      * This dialog shows current status, allows suspending (with optional end date) or unsuspending.
//      * @param user The UserDTO of the user to manage suspension for.
//      */
//     private void showManageSuspensionDialog(UserDTO user) {
//         Dialog dialog = new Dialog();
//         dialog.setHeaderTitle("Manage Suspension for " + user.getUserEmail() + " (ID: " + user.getUserId() + ")");
//         VerticalLayout dialogLayout = new VerticalLayout();
//         dialogLayout.setSpacing(true);
//         dialogLayout.setPadding(true);
//         dialogLayout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

//         Paragraph statusParagraph = new Paragraph("Fetching status...");
//         DatePicker endDatePicker = new DatePicker("End of Suspension (Optional)");
//         endDatePicker.setPlaceholder("YYYY-MM-DD (leave blank for permanent)");
//         endDatePicker.setMin(LocalDate.now()); // Cannot set suspension end date in the past

//         HorizontalLayout actionButtons = new HorizontalLayout();
//         Button suspendButton = new Button("Suspend");
//         Button unsuspendButton = new Button("Unsuspend");
//         Button closeButton = new Button("Close", e -> dialog.close());

//         actionButtons.add(suspendButton, unsuspendButton);
//         actionButtons.setJustifyContentMode(JustifyContentMode.END);
//         actionButtons.setWidthFull();

//         // Initially disable action buttons until status is fetched
//         suspendButton.setEnabled(false);
//         unsuspendButton.setEnabled(false);

//         dialogLayout.add(statusParagraph, endDatePicker, actionButtons);
//         dialog.getFooter().add(closeButton); // Add close button to the footer
//         dialog.add(dialogLayout);

//         dialog.open();

//         // Fetch the user's current suspension status and update UI elements
//         fetchAndDisplayUserSuspensionStatus(user.getUserId(), statusParagraph, suspendButton, unsuspendButton, endDatePicker);

//         // Add listeners for suspend/unsuspend actions
//         suspendButton.addClickListener(e -> {
//             LocalDate endDate = endDatePicker.getValue();
//             showConfirmDialog(
//                 "Confirm Suspension",
//                 "Are you sure you want to suspend " + user.getUserEmail() + (endDate != null ? " until " + endDate : " permanently") + "?",
//                 () -> {
//                     suspendUser(user.getUserId(), endDate);
//                     dialog.close(); // Close the main dialog after action
//                 }
//             );
//         });

//         unsuspendButton.addClickListener(e -> {
//             showConfirmDialog(
//                 "Confirm Unsuspension",
//                 "Are you sure you want to unsuspend " + user.getUserEmail() + "?",
//                 () -> {
//                     unsuspendUser(user.getUserId());
//                     dialog.close(); // Close the main dialog after action
//                 }
//             );
//         });
//     }

//     /**
//      * Helper to show a generic confirmation dialog.
//      */
//     private void showConfirmDialog(String title, String message, Runnable onConfirm) {
//         Dialog confirmDialog = new Dialog();
//         confirmDialog.setHeaderTitle(title);
//         confirmDialog.add(new Paragraph(message));
//         confirmDialog.getFooter().add(
//             new Button("Confirm", event -> {
//                 onConfirm.run();
//                 confirmDialog.close();
//             }),
//             new Button("Cancel", event -> confirmDialog.close())
//         );
//         confirmDialog.open();
//     }


//     /**
//      * Fetches and displays the current suspension status of a user within the dialog.
//      * @param userId The ID of the user.
//      * @param statusParagraph Paragraph to display status.
//      * @param suspendButton Button to enable/disable for suspension.
//      * @param unsuspendButton Button to enable/disable for unsuspension.
//      * @param endDatePicker DatePicker to show/set suspension end date.
//      */
//     private void fetchAndDisplayUserSuspensionStatus(int userId, Paragraph statusParagraph,
//                                                     Button suspendButton, Button unsuspendButton,
//                                                     DatePicker endDatePicker) {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }
//         // Requester ID is needed for backend authorization checks
//         int requesterId = currentUser.getUserId();

//         // API call to check if user is suspended
//         String isSuspendedUrl = backendUrl + "isUserSuspended/" + userId;
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token);
//         HttpEntity<Void> entity = new HttpEntity<>(headers);

//         try {
//             ResponseEntity<Response<Boolean>> isSuspendedResponse = restTemplate.exchange(
//                 isSuspendedUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<Boolean>>() {}
//             );

//             Response<Boolean> suspendedResult = isSuspendedResponse.getBody();
//             if (suspendedResult != null && suspendedResult.isSuccess()) {
//                 boolean isUserSuspended = suspendedResult.getData();
//                 statusParagraph.setText("Current Status: " + (isUserSuspended ? "Suspended" : "Active"));

//                 if (isUserSuspended) {
//                     suspendButton.setEnabled(false);    // Cannot suspend an already suspended user
//                     unsuspendButton.setEnabled(true);   // Can unsuspend

//                     // If suspended, try to fetch the end date of suspension
//                     String getEndDateUrl = backendUrl + "getSuspensionEndDate/" + requesterId + "/" + userId;
//                     ResponseEntity<Response<LocalDate>> endDateResponse = restTemplate.exchange(
//                         getEndDateUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<LocalDate>>() {}
//                     );
//                     Response<LocalDate> endDateResult = endDateResponse.getBody();

//                     if (endDateResult != null && endDateResult.isSuccess()) {
//                         LocalDate endDate = endDateResult.getData();
//                         if (endDate != null) {
//                             endDatePicker.setValue(endDate);
//                             statusParagraph.setText(statusParagraph.getText() + " until " + endDate);
//                         } else {
//                             statusParagraph.setText(statusParagraph.getText() + " (Permanent)");
//                         }
//                     } else {
//                         Notification.show("Could not fetch suspension end date.", 3000, Notification.Position.MIDDLE);
//                     }

//                 } else {
//                     suspendButton.setEnabled(true);     // Can suspend
//                     unsuspendButton.setEnabled(false);  // Cannot unsuspend an active user
//                     endDatePicker.setValue(null);       // Clear any previous end date
//                 }

//             } else {
//                 statusParagraph.setText("Could not determine suspension status.");
//                 Notification.show("Failed to get suspension status: " + (suspendedResult != null ? suspendedResult.getMessage() : "Unknown error"), 3000, Notification.Position.MIDDLE);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             statusParagraph.setText("Error fetching suspension status.");
//             Notification.show("Error fetching suspension status: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
//         }
//     }

//     /**
//      * Sends a request to the backend to suspend a user.
//      * @param userId The ID of the user to suspend.
//      * @param endOfSuspension Optional end date for the suspension.
//      */
//     private void suspendUser(int userId, LocalDate endOfSuspension) {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }
//         int requesterId = currentUser.getUserId();

//         // Endpoint: /suspendUser/{requesterId}/{userId}
//         String url = backendUrl + "suspendUser/" + requesterId + "/" + userId;

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token);
//         // Assuming the backend expects LocalDate as JSON in the request body for endOfSuspension.
//         // If it's a timestamp string or different format, adjust MediaType and HttpEntity.
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         HttpEntity<LocalDate> entity = new HttpEntity<>(endOfSuspension, headers); // Pass endOfSuspension as body

//         try {
//             ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
//                 url,
//                 HttpMethod.POST,
//                 entity,
//                 new ParameterizedTypeReference<Response<Void>>() {} // Response with Void data type
//             );

//             Response<Void> response = apiResponse.getBody();
//             if (response != null && response.isSuccess()) {
//                 Notification.show("User suspended successfully: " + response.getMessage(), 3000, Notification.Position.MIDDLE);
//                 fetchAllUsers(); // Refresh grid after successful suspension
//             } else {
//                 Notification.show("Failed to suspend user: " + (response != null ? response.getMessage() : "Unknown error"), 5000, Notification.Position.MIDDLE);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             Notification.show("Error suspending user: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
//         }
//     }

//     /**
//      * Sends a request to the backend to unsuspend a user.
//      * @param userId The ID of the user to unsuspend.
//      */
//     private void unsuspendUser(int userId) {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }
//         int requesterId = currentUser.getUserId();

//         // Endpoint: /unsuspendUser/{requesterId}/{userId}
//         String url = backendUrl + "unsuspendUser/" + requesterId + "/" + userId;

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token);
//         HttpEntity<Void> entity = new HttpEntity<>(headers); // No request body needed for unsuspend

//         try {
//             ResponseEntity<Response<Boolean>> apiResponse = restTemplate.exchange(
//                 url,
//                 HttpMethod.POST,
//                 entity,
//                 new ParameterizedTypeReference<Response<Boolean>>() {}
//             );

//             Response<Boolean> response = apiResponse.getBody();
//             if (response != null && response.isSuccess()) {
//                 Notification.show("User unsuspended successfully: " + response.getMessage(), 3000, Notification.Position.MIDDLE);
//                 fetchAllUsers(); // Refresh grid after successful unsuspension
//             } else {
//                 Notification.show("Failed to unsuspend user: " + (response != null ? response.getMessage() : "Unknown error"), 5000, Notification.Position.MIDDLE);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             Notification.show("Error unsuspending user: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
//         }
//     }

//     /**
//      * Sends a request to the backend to clean up expired suspensions.
//      */
//     private void cleanupExpiredSuspensions() {
//         String token = getTokenFromSession().orElse(null);
//         UserDTO currentUser = getUserDTOFromSession().orElse(null);

//         if (token == null || currentUser == null) {
//             Notification.show("Authentication error. Please log in.", 3000, Notification.Position.MIDDLE);
//             UI.getCurrent().navigate("login");
//             return;
//         }
//         int requesterId = currentUser.getUserId();

//         // Endpoint: /cleanupExpiredSuspensions/{requesterId}
//         String url = backendUrl + "cleanupExpiredSuspensions/" + requesterId;

//         HttpHeaders headers = new HttpHeaders();
//         headers.set("Authorization", token);
//         HttpEntity<Void> entity = new HttpEntity<>(headers);

//         try {
//             ResponseEntity<Response<Integer>> apiResponse = restTemplate.exchange(
//                 url,
//                 HttpMethod.POST,
//                 entity,
//                 new ParameterizedTypeReference<Response<Integer>>() {} // Response with Integer (count of cleaned suspensions)
//             );

//             Response<Integer> response = apiResponse.getBody();
//             if (response != null && response.isSuccess()) {
//                 Notification.show("Cleaned up " + response.getData() + " expired suspensions.", 3000, Notification.Position.MIDDLE);
//                 fetchAllUsers(); // Refresh grid to reflect changes
//             } else {
//                 Notification.show("Failed to cleanup expired suspensions: " + (response != null ? response.getMessage() : "Unknown error"), 5000, Notification.Position.MIDDLE);
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//             Notification.show("Error cleaning up expired suspensions: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
//         }
//     }
// }