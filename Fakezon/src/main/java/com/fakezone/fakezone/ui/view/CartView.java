package com.fakezone.fakezone.ui.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.RequestDataTypes.PurchaseRequest;
import DomainLayer.Enums.PaymentMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Route(value = "cart/:userId", layout = MainLayout.class)
public class CartView extends VerticalLayout implements AfterNavigationObserver{
   private int userId;
    private final String apiUrl;
    private final String webUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    private String token; // Store token for reuse
    private UserDTO currentUser; // Store current user for reuse

    // Layout to hold cart items, so we can easily clear and refresh
    private VerticalLayout cartContentLayout; 
    private H1 cartTitle;
    private H2 emptyCartMessage;

    public CartView(@Value("${api.url}") String apiUrl, @Value("${website.url}") String webUrl) {
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.restTemplate.setErrorHandler(new EmptyResponseErrorHandler()); // Set error handler once

        cartTitle = new H1("Your Cart");
        emptyCartMessage = new H2("Cart is Empty :(");
        emptyCartMessage.setVisible(false); // Initially hidden

        cartContentLayout = new VerticalLayout();
        cartContentLayout.setPadding(false);
        cartContentLayout.setSpacing(false);

        add(cartTitle, emptyCartMessage, cartContentLayout);
        setSizeFull(); // Ensure the main layout takes space
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        RouteParameters params = event.getRouteParameters();
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);

        if (session == null) {
            UI.getCurrent().navigate(webUrl);
            return;
        }

        this.token = (String) session.getAttribute("token");
        this.currentUser = (UserDTO) session.getAttribute("userDTO");

        try {
            this.userId = params.getInteger("userId").get();
            if (this.currentUser == null || this.currentUser.getUserId() != this.userId || this.token == null) {
                UI.getCurrent().navigate(webUrl); // if user tries to access a cart that's not theirs or not logged in
                return;
            }
        } catch (Exception e) {
            userIdFail();
            return;
        }
        loadCartData();
    }

    private void loadCartData() {
        cartContentLayout.removeAll(); // Clear previous content
        emptyCartMessage.setVisible(false);

        String url = apiUrl + "user/viewCart/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<List<CartItemInfoDTO>>> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Response<List<CartItemInfoDTO>>>() {
                    });
            Response<List<CartItemInfoDTO>> response = apiResponse.getBody();

            if (response != null && response.isSuccess()) {
                List<CartItemInfoDTO> cart = response.getData();
                if (cart == null || cart.isEmpty()) {
                    emptyCartMessage.setVisible(true);
                } else {
                    displayCartItems(cart);
                }
            } else {
                String errorMessage = response != null ? response.getMessage() : "Failed to load cart.";
                Notification.show(errorMessage, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                cartContentLayout.add(new H2(errorMessage));
            }
        } catch (Exception e) {
            Notification.show("Error connecting to the server. Please try again later.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cartContentLayout.add(new H2("Could not load cart."));
        }
    }

    private void displayCartItems(List<CartItemInfoDTO> cart) {
        Grid<CartItemInfoDTO> inStockGrid = new Grid<>(CartItemInfoDTO.class, false);
        inStockGrid.addColumn(CartItemInfoDTO::getProductName).setHeader("Product").setFlexGrow(2);
        inStockGrid.addColumn(CartItemInfoDTO::getStoreName).setHeader("Store").setFlexGrow(1);
        // Custom column for quantity
        inStockGrid.addComponentColumn(item -> createQuantityEditor(item))
                   .setHeader("Quantity")
                   .setKey("quantity-editor") // good practice to set a key
                   .setFlexGrow(1)
                   .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        inStockGrid.addComponentColumn(item -> createRemoveButton(item))
                .setHeader("Remove")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        inStockGrid.addColumn(item ->
                                String.format(Locale.US, "$%.2f",item.getUnitPrice())
                                ).setHeader("Price").setFlexGrow(0);
        Grid.Column<CartItemInfoDTO> inStockTotalCol = inStockGrid.addColumn(item ->
                                 String.format(Locale.US, "$%.2f", item.getUnitPrice() * item.getQuantityInCart()))
                            .setHeader("Total")
                            .setFlexGrow(0)
                            .setTextAlign(ColumnTextAlign.END)
                            .setComparator(Comparator.comparingDouble(
                                            item -> item.getUnitPrice() * item.getQuantityInCart()))
                            .setSortable(true);
        inStockGrid.sort(Collections.singletonList(new GridSortOrder<>(inStockTotalCol, SortDirection.DESCENDING)));  


        Grid<CartItemInfoDTO> outOfStockGrid = new Grid<>(CartItemInfoDTO.class, false);
        outOfStockGrid.addColumn(CartItemInfoDTO::getProductName).setHeader("Product").setFlexGrow(2);
        outOfStockGrid.addColumn(CartItemInfoDTO::getStoreName).setHeader("Store").setFlexGrow(1);
        outOfStockGrid.addComponentColumn(item -> createQuantityEditor(item))
                .setHeader("Quantity")
                .setKey("quantity-editor") // good practice to set a key
                .setFlexGrow(1)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        outOfStockGrid.addComponentColumn(item -> createRemoveButton(item))
                .setHeader("Remove")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.CENTER);
        outOfStockGrid.addColumn(item -> String.format(Locale.US, "$%.2f", item.getUnitPrice())).setHeader("Price")
                .setFlexGrow(0);
        Grid.Column<CartItemInfoDTO> outOfStockTotalCol = outOfStockGrid.addColumn(item -> String.format(Locale.US, "$%.2f", item.getUnitPrice() * item.getQuantityInCart()))
                .setHeader("Total")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END)
                .setComparator(Comparator.comparingDouble(
                        item -> item.getUnitPrice() * item.getQuantityInCart()))
                .setSortable(true);
        outOfStockGrid.sort(Collections.singletonList(new GridSortOrder<>(outOfStockTotalCol, SortDirection.DESCENDING)));

        List<CartItemInfoDTO> inStockProds = new ArrayList<>();
        List<CartItemInfoDTO> outOfStockProds = new ArrayList<>();

        for (CartItemInfoDTO item : cart) {
            if (item.isInStock()) {
                inStockProds.add(item);
            } else {
                outOfStockProds.add(item);
            }
        }

        if (!inStockProds.isEmpty()) {
            inStockGrid.setItems(inStockProds);
            cartContentLayout.add(inStockGrid);
        }
        if (!outOfStockProds.isEmpty()) {
            cartContentLayout.add(new H2("Items Out of Stock: "));
            outOfStockGrid.setItems(outOfStockProds);
            cartContentLayout.add(outOfStockGrid);
        }
        if (outOfStockProds.isEmpty() && !inStockProds.isEmpty()) {
            // 1) compute raw total as double
            double rawTotal = inStockProds.stream()
                    .mapToDouble(item -> item.getUnitPrice() * item.getQuantityInCart())
                    .sum();


            // 3) build footer layout
            HorizontalLayout totalsBar = new HorizontalLayout();
            totalsBar.getStyle().set("margin-top", "1em");
            totalsBar.setPadding(true);
            totalsBar.setAlignItems(Alignment.BASELINE);

            // raw total label
            NativeLabel totalLabel = new NativeLabel("Total:");
            totalLabel.getStyle().set("font-weight", "bold");
            NativeLabel totalValue = new NativeLabel(String.format(Locale.US, "$%.2f", rawTotal));
            totalsBar.add(totalLabel, totalValue);

            cartContentLayout.add(totalsBar);
            Button finalizeButton = new Button("Final Price & Purchase");
            finalizeButton.addClickListener(e -> purchaseDialog());
            cartContentLayout.add(finalizeButton);
        }
    }

    private void purchaseDialog() {
        // 1) Create dialog
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        
        // 2) Form fields
        TextField firstName = new TextField("First Name");
        TextField lastName  = new TextField("Last Name");
        DatePicker dob       = new DatePicker("Date of Birth");
        dob.setPlaceholder("YYYY-MM-DD");
        dialog.add(firstName,lastName,dob);
        Button calcDiscount = new Button("Check Final Price");
        calcDiscount.addClickListener(e -> {if(!firstName.isEmpty() && !lastName.isEmpty() && !dob.isEmpty()) { dialog.close();applyDiscount(firstName, lastName, dob);}});
        dialog.add(calcDiscount);
        dialog.open();

    }

    
    private void applyDiscount(TextField firstName, TextField lastName, DatePicker dob) {
       Dialog dialog = new Dialog();
       dialog.setWidth("60vw");
       firstName.setReadOnly(true);
       lastName.setReadOnly(true);
       dob.setReadOnly(true);
       dialog.add(firstName, lastName, dob);

       String url = apiUrl + "user/getCartFinalPrice/" + userId+"?dob="+dob.getValue().toString();
       // api call
       HttpHeaders headers = new HttpHeaders();
       headers.set("Authorization", token);
       HttpEntity<Void> entity = new HttpEntity<>(headers);
       ResponseEntity<Response<Double>> apiResponse = restTemplate.exchange(
               url,
               HttpMethod.GET,
               entity,
               new ParameterizedTypeReference<Response<Double>>() {
               });
       Response<Double> response = apiResponse.getBody();
       if(response.isSuccess()){
            ComboBox<PaymentMethod> paymentMethodComboBox = new ComboBox<>("Select Payment Method");
            paymentMethodComboBox.setItems(PaymentMethod.values());
            paymentMethodComboBox.setItemLabelGenerator(PaymentMethod::name);
            dialog.add(paymentMethodComboBox);
            TextField deliveryMethod = new TextField("Delivery Method");
            dialog.add(deliveryMethod);
            TextField cardNumber = new TextField("Credit Card Number");
            cardNumber.setAllowedCharPattern("\\d");
            TextField cardHolder = new TextField("Card Holder");
            
            DatePicker exp = new DatePicker("Expiry Date");
            exp.setPlaceholder("MM/YYYY"); 
            
            TextField cvv = new TextField("cvv");
            cvv.setAllowedCharPattern("\\d");
            cvv.setMaxLength(3);
            dialog.add(cardNumber, cardHolder, exp, cvv);

            // New fields for address details
            TextField streetAddress = new TextField("Street Address");
            streetAddress.setAllowedCharPattern("[^*]*"); // Restrict '*'
            TextField city = new TextField("City");
            city.setAllowedCharPattern("[^*]*"); // Restrict '*'
            ComboBox<String> countryComboBox = new ComboBox<>("Country");
            TextField zipCode = new TextField("Zip Code"); 
            zipCode.setAllowedCharPattern("[^*]*"); // Restrict '*'

            List<String> countryNames = Arrays.stream(Locale.getISOCountries())
                .map(code -> Locale.forLanguageTag("und-" + code).getDisplayCountry())
                .sorted()
                .collect(Collectors.toList());
            countryComboBox.setItems(countryNames);

            dialog.add(streetAddress, city, countryComboBox, zipCode); // Add all address components
            TextField packageDetails = new TextField("Package Details");
            dialog.add(packageDetails);
            
            NativeLabel totalLabel = new NativeLabel("Total:");
            totalLabel.getStyle().set("font-weight", "bold");
            NativeLabel totalValue = new NativeLabel(String.format(Locale.US, "$%.2f", response.getData()));
            VerticalLayout totalBox = new VerticalLayout(totalLabel, totalValue);
            totalBox.setPadding(false);
            totalBox.setSpacing(false);
            dialog.add(totalBox);
            Button confirmPurchase = new Button("Confirm Purchase");
            confirmPurchase.addClickListener(e ->{
                                if(!paymentMethodComboBox.isEmpty() && !deliveryMethod.isEmpty() && !cardNumber.isEmpty() &&
                                   !cardHolder.isEmpty() && !exp.isEmpty() && !cvv.isEmpty() && !streetAddress.isEmpty() && 
                                   !city.isEmpty() && !zipCode.isEmpty() && !countryComboBox.isEmpty() && !packageDetails.isEmpty()){
                                        dialog.close();
                                        confirmPurchase(firstName, lastName, dob, paymentMethodComboBox,
                                                        deliveryMethod, cardNumber, cardHolder, exp, cvv, 
                                                        streetAddress, city, countryComboBox, zipCode, packageDetails);
                                   }
            });
            dialog.add(confirmPurchase);

       }
       else{
            dialog.add(new H1(response.getMessage()));
            
       }
       dialog.open();

    }

    private void confirmPurchase(TextField firstName, TextField lastName, DatePicker dob, ComboBox<PaymentMethod> paymentMethodComboBox,
                                TextField deliveryMethod, TextField cardNumber, TextField cardHolder, DatePicker exp, TextField cvv, 
                                TextField streetAddress, TextField city, ComboBox<String> countryComboBox, TextField zipCode, TextField packageDetails){ 
        
        // Combine address components into a single fullAddress string using "*" as the separator
        String fullAddress = String.format("%s*%s*%s*%s", 
                                            streetAddress.getValue(), 
                                            city.getValue(), 
                                            countryComboBox.getValue(), 
                                            zipCode.getValue());

        // Format the expiry date to MM/YYYY
        String formattedExpDate = "";
        if (exp.getValue() != null) {
            formattedExpDate = String.format(Locale.US, "%02d/%d", exp.getValue().getMonthValue(), exp.getValue().getYear());
        }

        PurchaseRequest purReq = new PurchaseRequest(userId, 
                                                     getCountryCodeFromName(countryComboBox.getValue()), 
                                                     dob.getValue(), 
                                                     paymentMethodComboBox.getValue(), 
                                                     deliveryMethod.getValue(), 
                                                     cardNumber.getValue(), 
                                                     cardHolder.getValue(), 
                                                     formattedExpDate, // Use the formatted expiry date
                                                     cvv.getValue(), 
                                                     fullAddress, 
                                                     firstName.getValue() + " " + lastName.getValue(), 
                                                     packageDetails.getValue());
        
        Request<PurchaseRequest> apiReq = new Request<PurchaseRequest>(token, purReq);
        String url = apiUrl + "user/purchaseCart";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Request<PurchaseRequest>> entity = new HttpEntity<>(apiReq, headers);
        ResponseEntity<Response<String>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<String>>() {
        });
        Response<String> res = apiResponse.getBody();
        Dialog dialog = new Dialog();
        if(res.isSuccess()){
            dialog.add(new H1(res.getData()));
        }
        else{
            dialog.add(new H1(res.getMessage()));
        }
        loadCartData();
        dialog.open();
    }

    private String getCountryCodeFromName(String countryName) {
        for (String code : Locale.getISOCountries()) {
            Locale locale = new Locale("", code);
            if (locale.getDisplayCountry().equals(countryName)) {
                return code;
            }
        }
        return ""; // if no match is found
    }

    private HorizontalLayout createQuantityEditor(CartItemInfoDTO item) {
        int maxStock = getAvailableStock(item.getStoreId(), item.getProductId());
        IntegerField quantityField = new IntegerField();
        quantityField.setValue(item.getQuantityInCart());
        quantityField.setStep(1);
        quantityField.setMin(1); // Assuming quantity cannot be less than 1 (0 means remove)
        quantityField.setMax(maxStock);
        quantityField.setWidth("60px");
        quantityField.getElement().getStyle().set("text-align", "center");

        Button decreaseButton = new Button(new Icon(VaadinIcon.MINUS));
        Button increaseButton = new Button(new Icon(VaadinIcon.PLUS));
        Button confirmButton = new Button("Update"); // Or use an Icon like VaadinIcon.CHECK

        confirmButton.setVisible(false); // Initially hidden
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        decreaseButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        increaseButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);


        decreaseButton.addClickListener(e -> {
            int currentQuantity = quantityField.getValue();
            if (currentQuantity > 1) { // Or your defined minimum
                quantityField.setValue(currentQuantity - 1);
                confirmButton.setVisible(true);
            }
        });

        increaseButton.addClickListener(e -> {
            int currentQuantity = quantityField.getValue();
            if (currentQuantity < maxStock){
                quantityField.setValue(currentQuantity + 1);
                confirmButton.setVisible(true);
            }
        });

        confirmButton.addClickListener(e -> {
            int newQuantity = quantityField.getValue();
            // Prevent update if quantity hasn't changed (optional, but good UX)
            if (newQuantity == item.getQuantityInCart()) {
                confirmButton.setVisible(false);
                return;
            }
            updateCartItemQuantity(item.getProductId(), item.getStoreId(), newQuantity, confirmButton, quantityField, item.getQuantityInCart());
        });
        
        HorizontalLayout editorLayout = new HorizontalLayout(decreaseButton, quantityField, increaseButton, confirmButton);
        editorLayout.setAlignItems(Alignment.CENTER);
        editorLayout.setSpacing(false); // More compact
        return editorLayout;
    }

    private int getAvailableStock(int storeId, int productId){
        String url = apiUrl + "product/getProductFromStore/" + storeId +"/" + productId;
        // api call
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response<StoreProductDTO>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<StoreProductDTO>>() {
                });
        Response<StoreProductDTO> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData().getQuantity();
        }
        else{
            return -1;
        }
    }

    private void updateCartItemQuantity(int productId, int storeId, int newQuantity, 
                                        Button confirmButton, IntegerField quantityField, int originalQuantity) {

        String url = apiUrl + "user/addToBasket/" + this.userId +"/" + storeId + "/" + productId + "/" + newQuantity; 
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        confirmButton.setEnabled(false); // Disable while processing
        try {
            ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST, // Or POST, depending on your API design
                    entity,
                    new ParameterizedTypeReference<Response<Void>>() {
                    });

            Response<Void> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                Notification.show("Cart updated successfully!", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                loadCartData(); // Reload the entire cart view
            } else {
                String errorMessage = response != null ? response.getMessage() : "Failed to update cart.";
                Notification.show("Update failed: " + errorMessage, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                // Revert UI changes on failure
                quantityField.setValue(originalQuantity); 
                confirmButton.setVisible(false);
            }
        } catch (Exception ex) {
            // Log the exception
            Notification.show("Error updating cart: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            // Revert UI changes on failure
            quantityField.setValue(originalQuantity);
            confirmButton.setVisible(false);
        } finally {
            // Re-enable confirm button if it's still meant to be visible (e.g. if not hidden on success/failure)
            // In this case, loadCartData() rebuilds it, or it's hidden, so this might not be needed.
            // If you weren't hiding it, you'd do: confirmButton.setEnabled(true);
        }
    }

    private Button createRemoveButton(CartItemInfoDTO item) {
        Button btn = new Button(new Icon(VaadinIcon.TRASH));
        btn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        btn.getElement().setAttribute("title", "Remove from cart");
        btn.addClickListener(e -> {
            removeCartItem(item.getProductId(), item.getStoreId());
        });
        return btn;
    }

    private void removeCartItem(int productId, int storeId) {
        String url = apiUrl + "user/removeFromBasket/" + this.userId
                + "/" + storeId + "/" + productId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST, // or POST if your API uses POST
                    entity,
                    new ParameterizedTypeReference<Response<Void>>() {
                    });
            Response<Void> response = apiResponse.getBody();

            if (response.isSuccess()) {
                Notification.show("Item removed", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                // rebuild the grid in-place
                loadCartData();
            } else {
                String err = response != null ? response.getMessage() : "Remove failed";
                Notification.show(err, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void userIdFail() {
        cartContentLayout.removeAll(); // Clear any potential previous content
        cartTitle.setVisible(false);
        emptyCartMessage.setVisible(false);
        cartContentLayout.add(new H1("Illegal User Id"));
    }
}