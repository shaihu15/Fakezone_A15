package com.fakezone.fakezone.ui.view;

import java.util.ArrayList;
import java.util.List;
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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.Enums.StoreManagerPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Route(value = "store/:storeId", layout = MainLayout.class)
public class StorePageView extends VerticalLayout implements AfterNavigationObserver{
    private int storeId;
    private StoreDTO storeDto;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;
    private final String websiteUrl;
    private TextField searchField;
    private FlexLayout productsLayout;
    private List<StoreProductDTO> allProducts;
    private List<AuctionProductDTO> auctionProducts;
    private Dialog filterDialog;
    private NumberField minPriceField;
    private NumberField maxPriceField;
    private ComboBox<Double> productRatingBox;
    private ComboBox<String> categoryBox;
    private String currentSearch = "";
    private Double dialogMinPrice, dialogMaxPrice, dialogMinRating;
    private String dialogCategory;
    private HorizontalLayout topLayout = new HorizontalLayout();
    private final boolean isGuest;
    
    public StorePageView(@Value("${api.url}") String apiUrl, @Value("${website.url}") String websiteUrl){
        this.apiUrl = apiUrl;
        this.websiteUrl = websiteUrl;
        setPadding(true);
        setSpacing(true);
        topLayout.setWidthFull(); // Make topLayout take the full available width
        topLayout.setAlignItems(Alignment.BASELINE); // Align items vertically on their baseline
        topLayout.setSpacing(true); // Add some space between direct children of topLayout
        this.isGuest = isGuestToken();
        this.auctionProducts = new ArrayList<>();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        removeAll();
        // parse storeId from route
        RouteParameters params = event.getRouteParameters();
        this.storeId = Integer.parseInt(params.get("storeId").orElse("0"));

        // fetch DTO
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;
        this.storeDto = getStoreDTO(storeId, token);

        if (storeDto != null) {
            allProducts = new ArrayList<>(storeDto.getStoreProducts());
            if(!isGuest){
                auctionProducts = getAuctionProducts();
            }
            initFilterDialog();
            buildView();
        }
    }

    private void buildView() {
        // Title
        topLayout.add(new H1(storeDto.getName()));
        topLayout.add(new H2(String.valueOf(storeDto.getAverageRating())+ "★"));
        if(storeDto.isOpen()){
            if(!isGuest){
                Button addStoreRating = new Button("Rate Store");
                addStoreRating.addClickListener(e -> ratingDialog());
                topLayout.add(addStoreRating);

                if(hasPerms()){
                    topLayout.add(createManageButton());
                }
                topLayout.add(createMsgButton());
                topLayout.add(createOfferButton());
            }
            // 1) SET UP SEARCH FIELD
            searchField = new TextField();
            searchField.setPlaceholder("Search Store");
            searchField.setValueChangeMode(ValueChangeMode.LAZY);
            searchField.addValueChangeListener(e -> {
                currentSearch = e.getValue().trim().toLowerCase();
                refreshDisplay();
            });
            topLayout.add(searchField);

            Button openFilter = new Button("Filter", evt -> filterDialog.open());
            topLayout.add(openFilter);
            add(topLayout);
            // 2) CONTAINER FOR PRODUCT CARDS
            productsLayout = new FlexLayout();
            productsLayout.setFlexWrap(FlexWrap.WRAP);
            productsLayout.setJustifyContentMode(JustifyContentMode.START);
            productsLayout.setWidthFull();
            add(productsLayout);

            // 3) INITIAL POPULATE
            refreshDisplay();
        }
        else{
            if(isFounder()){
                topLayout.add(createManageButton());
            }
            add(topLayout);
            add(new H1("STORE IS CLOSED"));
        }
    }

     private void initFilterDialog() {
        filterDialog = new Dialog();
        minPriceField    = new NumberField("Min Price");
        maxPriceField    = new NumberField("Max Price");
        productRatingBox = new ComboBox<>("Product Rating");
        productRatingBox.setItems(0.0,1.0,2.0,3.0,4.0,5.0);
        categoryBox      = new ComboBox<>("Category");
        categoryBox.setItems(allProducts.stream()
            .map(sp->sp.getCategory().toString())
            .distinct().collect(Collectors.toList()));
        Button apply = new Button("Apply Filters", e -> {
            dialogMinPrice  = minPriceField.getValue();
            dialogMaxPrice  = maxPriceField.getValue();
            dialogMinRating = productRatingBox.getValue();
            dialogCategory  = categoryBox.getValue();
            filterDialog.close();
            refreshDisplay();
        });

        Button resetButton = new Button("Reset", e -> {
            minPriceField.clear();
            maxPriceField.clear();
            productRatingBox.clear();
            categoryBox.clear();

            dialogMinPrice  = null;
            dialogMaxPrice  = null;
            dialogMinRating = null;
            dialogCategory  = null;
            
            refreshDisplay();
            // Keep the dialog open for the user to see cleared fields or apply again
        });

        FormLayout form = new FormLayout(
            minPriceField, maxPriceField,
            productRatingBox, categoryBox, apply, resetButton
        );
        filterDialog.add(form);
    }

    private VerticalLayout buildProductCard(StoreProductDTO sp) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("flex", "0 0 30%")       // three cards per row; adjust 30% → 25% for four, etc.
            .set("box-sizing", "border-box")
            .set("margin", "0.5em");      // gutters
        if(sp.getQuantity() == 0){
            card.getStyle()
                .set("opacity", "0.6")
                .set("background", "#f8f8f8")
                .set("pointer-events", "none");
        }
        RouterLink nameLink = new RouterLink(
            sp.getName(),
            StoreProductView.class,
            new RouteParameters(
                new RouteParam("storeId", Integer.toString(storeId)),
                new RouteParam("productId", Integer.toString(sp.getProductId()))
            )
        );

        nameLink.getStyle()
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("font-weight", "600")
            .set("text-decoration", "none");
        card.add(nameLink);

    
        card.add(new H5(sp.getCategory().toString()));
        card.add(new H5("Rating: " + sp.getAverageRating()));

        Span price = new Span("Price: $" + String.format("%.2f", sp.getBasePrice()));
        price.getStyle().set("font-weight", "bold");
        card.add(price);

        IntegerField qtyField = new IntegerField("Quantity:");
        qtyField.setStepButtonsVisible(true);
        qtyField.setMin(1);
        qtyField.setMax(sp.getQuantity());
        qtyField.setValue(Math.min(1, sp.getQuantity()));
        qtyField.setWidth("100px");

        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.setSize("30px");
        Button cartButton = new Button(cartIcon);
        cartButton.addClickListener(evt ->
            addToCart(sp.getStoreId(), sp.getProductId(), qtyField.getValue())
        );

        HorizontalLayout controls = new HorizontalLayout(qtyField, cartButton);
        controls.setAlignItems(Alignment.END);
        controls.setSpacing(true);
        controls.setPadding(false);
        card.add(controls);


        return card;
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

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                return response.getBody().getData();
            } else {
                Notification.show("Failed to load store with ID: " + storeId + ". Reason: " +
                                  (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"),
                                  3000, Notification.Position.MIDDLE);
                return null;
            }
        } catch (Exception e) {
            Notification.show("Error fetching store with ID: " + storeId + ": " + e.getMessage(),
                               3000, Notification.Position.MIDDLE);
            return null;
        }
    }

    private boolean isFounder(){
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        return user != null && user.getUserId() == storeDto.getFounderId();
    }

    private void addToCart(int storeId, int prodId, int quantity){
        if(quantity == 0){
            Notification.show("Has to be at least 1");
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        UserDTO userDto = (UserDTO) session.getAttribute("userDTO");
        int userId = userDto.getUserId();
        String url = apiUrl + "user/addToBasket/" + userId +"/"+storeId+"/"+prodId+"/"+quantity;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response<String>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Response<String>>() {}
        );            
        Response<String> response = apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Added to Cart succefully");
        }
        else{
            Notification.show(response.getMessage());
        }
    }

     private void refreshDisplay() {
        productsLayout.removeAll();

        List<StoreProductDTO> filteredProducts = allProducts.stream()
            // 1) search filter
            .filter(sp -> sp.getName().toLowerCase().contains(currentSearch)
                       || sp.getCategory().toString().toLowerCase().contains(currentSearch))
            // 2) dialog filters
            .filter(sp -> dialogMinPrice  == null || sp.getBasePrice()     >= dialogMinPrice)
            .filter(sp -> dialogMaxPrice  == null || sp.getBasePrice()     <= dialogMaxPrice)
            .filter(sp -> dialogMinRating == null || sp.getAverageRating() >= dialogMinRating)
            .filter(sp -> dialogCategory  == null || sp.getCategory().toString().equals(dialogCategory))
            .collect(Collectors.toList());

        filteredProducts.forEach(sp -> productsLayout.add(buildProductCard(sp)));
        
        List<AuctionProductDTO> filteredAuctions = new ArrayList<>();
        if (!isGuest && auctionProducts != null && !auctionProducts.isEmpty()) {
            filteredAuctions = auctionProducts.stream()
            .filter(ap -> ap.getName().toLowerCase().contains(currentSearch))
            .filter(ap -> dialogMinPrice  == null || ap.getCurrentHighestBid() >= dialogMinPrice)
            .filter(ap -> dialogMaxPrice  == null || ap.getCurrentHighestBid() <= dialogMaxPrice)
            .filter(ap -> dialogMinRating == null || ap.getAverageRating()    >= dialogMinRating)
            .filter(ap -> dialogCategory  == null || ap.getCategory().toString().equals(dialogCategory))
            .collect(Collectors.toList());
        }
        filteredAuctions.forEach(ap   -> productsLayout.add(buildAuctionProductCard(ap)));
        if (filteredProducts.isEmpty() && filteredAuctions.isEmpty()) {
            productsLayout.add(new Span("No products match your criteria."));
        }
    }


    private void ratingDialog(){
        Dialog dialog = new Dialog();
        TextField comment = new TextField();
        dialog.add(new H2("Review:"));
        dialog.add(comment);
        dialog.add(new H2("Score:"));
        TextField rate = new TextField("1-5");
        rate.setAllowedCharPattern("\\d");
        dialog.add(rate);
        Button send = new Button("Send Review");
        send.addClickListener(e ->{
            try{
                int rating = Integer.parseInt(rate.getValue());
                if(!(comment.isEmpty() || rate.isEmpty() || rating > 5 || rating < 1)){
                    dialog.close();
                    sendRating(comment.getValue(), rating);
                }
            }
            catch(Exception ex){
                //do nothing
            }
        });
        dialog.add(send);
        dialog.open();
    }

    private void sendRating(String comment, int rating){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String url = apiUrl + "/store/ratingStore/" + storeId + "/" + user.getUserId() +"?rating="+ ((double) rating) + "&comment=" + comment;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<Response<Void>>() {});
        Response<Void> response = apiResponse.getBody();
        Dialog dialog = new Dialog();
        dialog.add(new H2(response.getMessage()));
        dialog.open();

    }

    private boolean isGuestToken(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        String url = apiUrl + "user/isGuestToken";
        ResponseEntity<Response<Boolean>> apiResponse = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(token), new ParameterizedTypeReference<Response<Boolean>>() {});
        
        Response<Boolean> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData();
        }
        else{
            Notification.show(response.getMessage());
            return true;
        }
    }

    private Button createManageButton(){
        Button manage = new Button("Manage Store");
        manage.addClickListener(e -> UI.getCurrent().navigate("store/" + storeId + "/manage"));
        return manage;
    }

    private boolean hasPerms(){
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
            if(response.getData())
                return true;
            else{
                url = apiUrl + "/store/isStoreManager/" + storeId + "/" + user.getUserId();
                ResponseEntity<Response<List<StoreManagerPermission>>> apiResponseManager = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<List<StoreManagerPermission>>>() {});
                Response<List<StoreManagerPermission>> resp = apiResponseManager.getBody();
                if(resp.isSuccess()){
                    return resp.getData() != null;
                }
                else{
                    Notification.show(resp.getMessage());
                    return false;
                }

            }
        }
        else{
            Notification.show(response.getMessage());
            return false;
        }
    }

    private List<AuctionProductDTO> getAuctionProducts(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String url = apiUrl + "store/getAuctionProductsFromStore/" + storeId + "/" + user.getUserId();
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<List<AuctionProductDTO>>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            new ParameterizedTypeReference<Response<List<AuctionProductDTO>>>() {});
        Response<List<AuctionProductDTO>> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData();
        }
        else{
            Notification.show(response.getMessage());
            return null;
        }
    }

    private VerticalLayout buildAuctionProductCard(AuctionProductDTO ap) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
            .set("flex", "0 0 30%")       // three cards per row; adjust 30% → 25% for four, etc.
            .set("box-sizing", "border-box")
            .set("margin", "0.5em");      // gutters
        H4 name = new H4(ap.getName());
        name.getStyle()
            .set("font-size", "var(--lumo-font-size-xl)")
            .set("font-weight", "600")
            .set("text-decoration", "none");
        card.add(name);

    
        card.add(new H5(ap.getCategory().toString()));
        
        // Find the corresponding StoreProduct for rating information
        // Note: AuctionProduct.getProductId() returns the original StoreProduct's ID
        List<StoreProductDTO> matchingProducts = allProducts.stream()
            .filter(sp -> sp.getProductId() == ap.getProductId())
            .toList();
            
        if (!matchingProducts.isEmpty()) {
            StoreProductDTO ratedProd = matchingProducts.get(0);
            double rating = ratedProd.getAverageRating();
            card.add(new H5("Rating: " + rating));
        } else {
            // If no matching product found, show default rating message
            card.add(new H5("Rating: Not available"));
        }

        Span price = new Span("Price: $" + String.format("%.2f", ap.getCurrentHighestBid()));
        price.getStyle().set("font-weight", "bold");
        card.add(price);

        Button bidButton = new Button("Place Bid");
        bidButton.addClickListener(e -> addBidDialog(ap.getProductId()));
        bidButton.setEnabled(!ap.isDone()); // enabled only if the bid is not done
        card.add(bidButton);
        return card;
    }
    
    private void addBidDialog(int productId){
        Dialog dialog = new Dialog();
        NumberField amount = new NumberField("Bid Amount");
        Button bidButton = new Button("Place Bid", e -> {
                                        if(amount.getValue() != null && !amount.isEmpty()){
                                            placeBid(productId, amount.getValue());
                                            dialog.close();
                                        }}
                                    );
        
        dialog.add(amount);
        dialog.add(bidButton);
        dialog.open();
    }

    private void placeBid(int prodId, Double amount){
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String token = (String) session.getAttribute("token");
        String url = apiUrl + "store/addBidOnAuctionProductInStore/" + storeId +"/" + user.getUserId() + "/" + prodId + "?bid=" + amount;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(header);
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
            url, 
            HttpMethod.POST, 
            entity, 
            new ParameterizedTypeReference<Response<Void>>() {});
        Response<Void> response = apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Bid Placed");
        }
        else{
            Notification.show(response.getMessage());
        }

    }

    private Button createMsgButton(){
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String token = (String) session.getAttribute("token");
        int userId = user.getUserId();
        Button msgButton = new Button(new Icon(VaadinIcon.ENVELOPE));
        msgButton.getStyle().set("font-size", "24px")
            .set("color", "#0077cc")
            .set("background-color", "transparent")
            .set("border", "none");
        msgButton.addClickListener(e -> msgDialog(userId, storeId, token));
        return msgButton;
    }

    private void msgDialog(int userId, int storeId, String token){
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
        String url = apiUrl + "user/sendMessageToStore/" + userId + "/" + storeId;
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

    private Button createOfferButton(){
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        String token = (String) session.getAttribute("token");
        int userId = user.getUserId();
        Button ofrButton = new Button("Place Offer");
        ofrButton.addClickListener(e -> ofrDialog(storeId, userId, token));
        return ofrButton;
    }

    private void ofrDialog(int storeId, int userId, String token){
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        ComboBox<StoreProductDTO> productCombo = new ComboBox<>("Select product");
        productCombo.setItems(allProducts); // set your DTOs
        productCombo.setItemLabelGenerator(StoreProductDTO::getName);
        productCombo.setPlaceholder("— choose one —");
        productCombo.setWidthFull();
        dialog.add(productCombo);
        NumberField offerAmountField = new NumberField("Your Offer");
        dialog.add(offerAmountField);
        Button send = new Button("Send", e -> {
            StoreProductDTO selected = productCombo.getValue();
            Double offer = offerAmountField.getValue();
            
            if (selected == null) {
                productCombo.focus();
                Notification.show("Please pick a product").setPosition(Notification.Position.MIDDLE);
                return;
            }
            if (offer == null || offer <= 0) {
                offerAmountField.focus();
                Notification.show("Please enter a valid offer").setPosition(Notification.Position.MIDDLE);
                return;
            }
            int selectedProductId = selected.getProductId();
            placeOffer(storeId, userId, selectedProductId, offer, token);
            dialog.close();
        });
        dialog.add(send);
        dialog.open();
    }
    
    private void placeOffer(int storeId, int userId, int productId, double offer, String token){
        String url = apiUrl + "store/placeOfferOnStoreProduct/" + storeId + "/" + userId + "/" + productId
                + "?offerAmount=" + offer;
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
            Notification.show("Offer Submitted Successfully");
        } else {
            Notification.show(response.getMessage());
        }
    }

}


