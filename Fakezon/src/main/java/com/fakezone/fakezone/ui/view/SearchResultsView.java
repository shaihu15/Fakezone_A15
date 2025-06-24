package com.fakezone.fakezone.ui.view;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.vaadin.flow.component.grid.Grid;

// Custom class to represent a product-store combination for filtered results
class FilteredProductResult {
    private ProductDTO product;
    private Set<Integer> matchingStoreIds;
    
    public FilteredProductResult(ProductDTO product, Set<Integer> matchingStoreIds) {
        this.product = product;
        this.matchingStoreIds = matchingStoreIds;
    }
    
    public ProductDTO getProduct() { return product; }
    public Set<Integer> getMatchingStoreIds() { return matchingStoreIds; }
    
    // Delegate methods for grid columns
    public String getName() { return product.getName(); }
    public PCategory getCategory() { return product.getCategory(); }
    public String getDescription() { return product.getDescription(); }
    public int getId() { return product.getId(); }
}

@Route(value = "search", layout = MainLayout.class)
public class SearchResultsView extends VerticalLayout implements BeforeEnterObserver {
    private Grid<FilteredProductResult> grid;
    private List<ProductDTO> allProducts;
    private List<StoreProductDTO> storeProductData;
    private Map<Integer, StoreDTO> storeData;
    private Button toggleStoreSearchButton;
    private HorizontalLayout storeSearchLayout;
    private boolean isStoreSearchVisible = false;
    
    // Store-specific search fields
    private boolean isStoreSpecificSearch = false;
    private Integer targetStoreId = null;
    private String targetStoreName = null;
    
    // UI Components for store-specific search
    private ComboBox<StoreDTO> storeSelector;
    private ComboBox<String> searchTypeSelector;
    private TextField searchTermField;
    private Button searchButton;

    public SearchResultsView() {
        grid = new Grid<>(FilteredProductResult.class);
        
        // Create store-specific search UI
        createStoreSpecificSearchUI();
        
        // Add components to layout
        add(createSearchHeader());
        add(grid);
        
        Button filterButton = new Button(VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFilterDialog());
        add(filterButton);
    }
    
    private VerticalLayout createSearchHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(true);
        header.setPadding(false);
        
        // Title
        Span title = new Span("Product Search");
        title.getStyle().set("font-size", "1.5em");
        title.getStyle().set("font-weight", "bold");
        header.add(title);
        
        // Toggle button for store search
        header.add(toggleStoreSearchButton);
        
        // Store-specific search section (collapsible)
        header.add(storeSearchLayout);
        
        return header;
    }
    
    private void createStoreSpecificSearchUI() {
        // Store selector
        storeSelector = new ComboBox<>("Select Store");
        storeSelector.setPlaceholder("Choose a store...");
        storeSelector.setItemLabelGenerator(store -> 
            store.getName() + " (ID: " + store.getStoreId() + ")");
        storeSelector.setWidth("250px");
        
        // Search type selector
        searchTypeSelector = new ComboBox<>("Search Type");
        searchTypeSelector.setItems("keyword", "category", "product_name");
        searchTypeSelector.setValue("keyword");
        searchTypeSelector.setWidth("150px");
        
        // Search term field
        searchTermField = new TextField("Search Term");
        searchTermField.setPlaceholder("Enter search term...");
        searchTermField.setWidth("200px");
        
        // Add enter key listener to search term field
        searchTermField.getElement().addEventListener("keydown", e -> {
            if (e.getEventData().getString("event.key").equals("Enter")) {
                performStoreSpecificSearch();
            }
        }).addEventData("event.key");
        
        // Search button
        searchButton = new Button("Search in Store", VaadinIcon.SEARCH.create());
        searchButton.addClickListener(e -> performStoreSpecificSearch());
        
        // Toggle button for store search
        toggleStoreSearchButton = new Button("Advanced Store Search", VaadinIcon.ANGLE_DOWN.create());
        toggleStoreSearchButton.addClickListener(e -> toggleStoreSearch());
        
        // Create the collapsible store search layout
        storeSearchLayout = new HorizontalLayout();
        storeSearchLayout.setSpacing(true);
        storeSearchLayout.setAlignItems(Alignment.END); // Align components to bottom for same height
        storeSearchLayout.add(storeSelector, searchTypeSelector, searchTermField, searchButton);
        storeSearchLayout.setVisible(false); // Initially hidden
        
        // Load available stores
        loadAvailableStores();
    }
    
    private void toggleStoreSearch() {
        isStoreSearchVisible = !isStoreSearchVisible;
        storeSearchLayout.setVisible(isStoreSearchVisible);
        
        if (isStoreSearchVisible) {
            toggleStoreSearchButton.setText("Hide Store Search");
            toggleStoreSearchButton.setIcon(VaadinIcon.ANGLE_UP.create());
        } else {
            toggleStoreSearchButton.setText("Advanced Store Search");
            toggleStoreSearchButton.setIcon(VaadinIcon.ANGLE_DOWN.create());
        }
    }
    
    private void loadAvailableStores() {
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        try {
            // Assuming you have an endpoint to get all stores
            String url = "http://localhost:8080/api/store/getAllStores";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<StoreDTO>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<StoreDTO>>>() {}
            );

            Response<List<StoreDTO>> body = response.getBody();
            if (body != null && body.isSuccess() && body.getData() != null) {
                List<StoreDTO> stores = body.getData();
                storeSelector.setItems(stores);
            } else {
                Notification.show("Failed to load stores.");
            }
        } catch (Exception e) {
            Notification.show("Error loading stores: " + e.getMessage());
        }
    }
    
    private void performStoreSpecificSearch() {
        StoreDTO selectedStore = storeSelector.getValue();
        String searchType = searchTypeSelector.getValue();
        String searchTerm = searchTermField.getValue();
        
        if (selectedStore == null) {
            Notification.show("Please select a store.");
            return;
        }
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            Notification.show("Please enter a search term.");
            return;
        }
        
        // Perform store-specific search
        switch (searchType.toLowerCase()) {
            case "category":
                searchProductsInStoreByCategory(selectedStore.getStoreId(), selectedStore.getName(), searchTerm.trim());
                break;
            case "product_name":
                searchProductsInStoreByName(selectedStore.getStoreId(), selectedStore.getName(), searchTerm.trim());
                break;
            case "keyword":
            default:
                searchProductsInStoreByKeyword(selectedStore.getStoreId(), selectedStore.getName(), searchTerm.trim());
                break;
        }
    }

    private void configureGrid() {
        grid.removeAllColumns();
        
        // Add columns with auto-width
        grid.addColumn(FilteredProductResult::getName)
            .setHeader("Name")
            .setAutoWidth(true);
        
        grid.addColumn(p -> p.getCategory().name())
            .setHeader("Category")
            .setAutoWidth(true);
        
        grid.addColumn(FilteredProductResult::getDescription)
            .setHeader("Description")
            .setAutoWidth(true);
        
        // For store-specific search, show clickable store name
        if (isStoreSpecificSearch) {
            grid.addColumn(new ComponentRenderer<>(p -> {
                StoreDTO store = storeData.get(targetStoreId);
                if (store != null) {
                    Anchor storeLink = new Anchor();
                    storeLink.setText(store.getName());
                    storeLink.getStyle().set("text-decoration", "none");
                    storeLink.getStyle().set("color", "#1976d2");
                    storeLink.getStyle().set("cursor", "pointer");
                    
                    storeLink.getElement().addEventListener("click", e -> {
                        UI.getCurrent().navigate("store/" + targetStoreId);
                    });
                    
                    return storeLink;
                } else {
                    return new Span(targetStoreName != null ? targetStoreName : "Store");
                }
            }))
                .setHeader("Store")
                .setAutoWidth(true);
        } else {
            grid.addColumn(new ComponentRenderer<>(p -> getStoresSummaryComponent(p, storeProductData, storeData)))
                .setHeader("Available in Stores")
                .setAutoWidth(true);
        }
        
        grid.addColumn(p -> getPrice(p, storeProductData))
            .setHeader("Price")
            .setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(p -> {
            Optional<StoreProductDTO> sp = storeProductData.stream()
                .filter(dto -> dto.getProductId() == p.getId() && 
                            p.getMatchingStoreIds().contains(dto.getStoreId()))
                .findFirst();
            return sp.map(dto -> createSimpleStarRating(dto.getAverageRating()))
                    .orElse(new Span("N/A"));
        }))
            .setHeader("Product Rating")
            .setAutoWidth(true);
        
        // Only show store rating column for general search (not store-specific)
        if (!isStoreSpecificSearch) {
            grid.addColumn(new ComponentRenderer<>(p -> {
                List<Integer> storeIds = new ArrayList<>(p.getMatchingStoreIds());
                if (storeIds.isEmpty()) return new Span("N/A");
                
                Span container = new Span();
                for (int i = 0; i < storeIds.size(); i++) {
                    Integer storeId = storeIds.get(i);
                    StoreDTO store = storeData.get(storeId);
                    if (store != null) {
                        // Create clickable store name
                        Anchor storeLink = new Anchor();
                        storeLink.setText(store.getName());
                        storeLink.getStyle().set("text-decoration", "none");
                        storeLink.getStyle().set("color", "#1976d2");
                        storeLink.getStyle().set("cursor", "pointer");
                        
                        storeLink.getElement().addEventListener("click", e -> {
                            UI.getCurrent().navigate("store/" + storeId);
                        });
                        
                        container.add(storeLink);
                        container.add(new Span(" "));
                        container.add(createSimpleStarRating(store.getAverageRating()));
                        
                        if (i < storeIds.size() - 1) {
                            container.add(new Span(", "));
                        }
                    }
                }
                return container;
            }))
                .setHeader("Store Rating")
                .setAutoWidth(true);
        } else {
            // For store-specific search, show the store rating
            grid.addColumn(new ComponentRenderer<>(p -> {
                StoreDTO store = storeData.get(targetStoreId);
                if (store != null) {
                    return createSimpleStarRating(store.getAverageRating());
                } else {
                    return new Span("N/A");
                }
            }))
                .setHeader("Store Rating")
                .setAutoWidth(true);
        }
        
        // Set the grid to use full width
        grid.setWidthFull();
    }

    // Updated helper method to create simple star rating component (just rating + one star)
    private Span createSimpleStarRating(double rating) {
        Span container = new Span();
        
        // Add numeric rating
        Span numericRating = new Span(String.format("%.1f", rating));
        numericRating.getStyle().set("color", "black");
        container.add(numericRating);
        
        // Add single yellow star
        Span star = new Span(" ★");
        star.getStyle().set("color", "#FFD700"); // Gold/Yellow
        container.add(star);
        
        return container;
    }

    // Helper method to format rating as text with yellow star (for store summary)
    private String formatRatingText(double rating) {
        return String.format("%.1f ★", rating);
    }

    // Helper method to create full star rating for filter dialog (filled + empty stars)
    private Span createFullStarRating(int rating) {
        Span container = new Span();
        
        // Add filled stars (black)
        for (int i = 0; i < rating; i++) {
            Span filledStar = new Span("★");
            filledStar.getStyle().set("color", "black");
            container.add(filledStar);
        }
        
        // Add empty stars (white with black outline)
        for (int i = rating; i < 5; i++) {
            Span emptyStar = new Span("☆");
            emptyStar.getStyle().set("color", "white");
            emptyStar.getStyle().set("text-shadow", "0 0 1px black");
            container.add(emptyStar);
        }
        
        return container;
    }

    private void openFilterDialog() {
        Dialog filterDialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        filterDialog.open();

        NumberField minPrice = new NumberField("Min Price");
        NumberField maxPrice = new NumberField("Max Price");

        ComboBox<Integer> productRating = new ComboBox<>("Product Rating", List.of(1, 2, 3, 4, 5));
        productRating.setRenderer(new ComponentRenderer<>(rating -> {
            Span container = new Span();
            container.add(new Span(rating + " "));
            container.add(createFullStarRating(rating));
            return container;
        }));
        productRating.setItemLabelGenerator(r -> r + " Stars and above");

        // Only show store rating filter for general search (not store-specific)
        ComboBox<Integer> storeRating = null;
        if (!isStoreSpecificSearch) {
            storeRating = new ComboBox<>("Store Rating", List.of(1, 2, 3, 4, 5));
            storeRating.setRenderer(new ComponentRenderer<>(rating -> {
                Span container = new Span();
                container.add(new Span(rating + " "));
                container.add(createFullStarRating(rating));
                return container;
            }));
            storeRating.setItemLabelGenerator(r -> r + " Stars and above");
        }

        ComboBox<String> category = new ComboBox<>("Category");
        category.setItems(Arrays.stream(PCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList()));

        ComboBox<Integer> finalStoreRating = storeRating; // For lambda usage
        Button apply = new Button("Apply Filters", e -> {
            if (allProducts == null) {
                Notification.show("No products to filter.");
                return;
            }

            Double min = minPrice.getValue();
            Double max = maxPrice.getValue();
            Integer selectedProductRating = productRating.getValue();
            Integer selectedStoreRating = finalStoreRating != null ? finalStoreRating.getValue() : null;
            String selectedCategory = category.getValue();

            HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                Notification.show("Session expired.");
                UI.getCurrent().navigate(HomeView.class);
                return;
            }
            String token = (String) session.getAttribute("token");

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

            List<FilteredProductResult> filtered = allProducts.stream()
                .map(product -> {
                    Set<Integer> matchingStores = product.getStoreIds().stream()
                        .filter(storeId -> {
                            // For store-specific search, only consider the target store
                            if (isStoreSpecificSearch && !storeId.equals(targetStoreId)) {
                                return false;
                            }
                            
                            try {
                                String url = String.format("http://localhost:8080/api/product/getProductFromStore/%d/%d", storeId, product.getId());
                                HttpHeaders headers = new HttpHeaders();
                                headers.set("Authorization", token);
                                HttpEntity<Void> entity = new HttpEntity<>(headers);

                                ResponseEntity<Response<StoreProductDTO>> response = restTemplate.exchange(
                                        url,
                                        HttpMethod.GET,
                                        entity,
                                        new ParameterizedTypeReference<Response<StoreProductDTO>>() {
                                        });

                                Response<StoreProductDTO> body = response.getBody();
                                if (body == null || !body.isSuccess()) return false;

                                StoreProductDTO dto = body.getData();

                                boolean priceOk = (min == null || dto.getBasePrice() >= min) &&
                                                (max == null || dto.getBasePrice() <= max);

                                boolean productRatingOk = selectedProductRating == null ||
                                                        dto.getAverageRating() >= selectedProductRating;

                                boolean categoryOk = selectedCategory == null ||
                                                    dto.getCategory().name().equalsIgnoreCase(selectedCategory);

                                // Store rating filter only applies to general search
                                boolean storeRatingOk = true;
                                if (!isStoreSpecificSearch && selectedStoreRating != null) {
                                    String storeUrl = String.format("http://localhost:8080/api/store/viewStore/%d", storeId);
                                    ResponseEntity<Response<StoreDTO>> storeResponse = restTemplate.exchange(
                                            storeUrl,
                                            HttpMethod.GET,
                                            entity,
                                            new ParameterizedTypeReference<Response<StoreDTO>>() {
                                            });

                                    Response<StoreDTO> storeBody = storeResponse.getBody();
                                    if (storeBody == null || !storeBody.isSuccess()) return false;
                                    StoreDTO storeDTO = storeBody.getData();
                                    
                                    storeRatingOk = storeDTO.getAverageRating() >= selectedStoreRating;
                                }

                                return priceOk && productRatingOk && storeRatingOk && categoryOk;

                            } catch (Exception ex) {
                                return false;
                            }
                        }).collect(Collectors.toSet());

                    return matchingStores.isEmpty() ? null : new FilteredProductResult(product, matchingStores);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            grid.setItems(filtered);
            filterDialog.close();
        });

        // Add components to layout - conditionally add store rating
        layout.add(minPrice, maxPrice, productRating);
        if (storeRating != null) {
            layout.add(storeRating);
        }
        layout.add(category, apply);
        filterDialog.add(layout);
    }

    // Updated store-specific search methods to use store ID directly
    private void searchProductsInStoreByKeyword(Integer storeId, String storeName, String keyword) {
        this.isStoreSpecificSearch = true;
        this.targetStoreId = storeId;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        // First, fetch the store details to get the correct store name
        try {
            String storeUrl = String.format("http://localhost:8080/api/store/viewStore/%d", storeId);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<StoreDTO>> storeResponse = restTemplate.exchange(
                storeUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<StoreDTO>>() {}
            );

            Response<StoreDTO> storeBody = storeResponse.getBody();
            if (storeBody != null && storeBody.isSuccess() && storeBody.getData() != null) {
                this.targetStoreName = storeBody.getData().getName();
            } else {
                this.targetStoreName = storeName; // Fallback to provided name
            }
        } catch (Exception e) {
            this.targetStoreName = storeName; // Fallback to provided name
        }

        String url = "http://localhost:8080/api/product/searchProducts/keyword/" + keyword;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                
                // Filter products to only include those available in the target store
                List<ProductDTO> storeProducts = products.stream()
                    .filter(p -> p.getStoreIds().contains(storeId))
                    .collect(Collectors.toList());
                
                this.allProducts = storeProducts;

                // Fetch additional data for rendering columns
                this.storeProductData = fetchStoreProductData(storeProducts, token);
                this.storeData = fetchStoreData(storeProductData, token);

                // Convert to FilteredProductResult with only the target store
                List<FilteredProductResult> results = storeProducts.stream()
                    .map(p -> new FilteredProductResult(p, Set.of(storeId)))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
                
                if (results.isEmpty()) {
                    Notification.show("No products found matching '" + keyword + "' in store '" + targetStoreName + "'.");
                }
            } else {
                Notification.show(response != null ? response.getMessage() : "Search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchProductsInStoreByCategory(Integer storeId, String storeName, String category) {
        this.isStoreSpecificSearch = true;
        this.targetStoreId = storeId;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        // First, fetch the store details to get the correct store name
        try {
            String storeUrl = String.format("http://localhost:8080/api/store/viewStore/%d", storeId);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<StoreDTO>> storeResponse = restTemplate.exchange(
                storeUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<StoreDTO>>() {}
            );

            Response<StoreDTO> storeBody = storeResponse.getBody();
            if (storeBody != null && storeBody.isSuccess() && storeBody.getData() != null) {
                this.targetStoreName = storeBody.getData().getName();
            } else {
                this.targetStoreName = storeName; // Fallback to provided name
            }
        } catch (Exception e) {
            this.targetStoreName = storeName; // Fallback to provided name
        }

        String url = "http://localhost:8080/api/product/searchProducts/category/" + category;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                
                List<ProductDTO> storeProducts = products.stream()
                    .filter(p -> p.getStoreIds().contains(storeId))
                    .collect(Collectors.toList());
                
                this.allProducts = storeProducts;
                this.storeProductData = fetchStoreProductData(storeProducts, token);
                this.storeData = fetchStoreData(storeProductData, token);

                List<FilteredProductResult> results = storeProducts.stream()
                    .map(p -> new FilteredProductResult(p, Set.of(storeId)))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
                
                if (results.isEmpty()) {
                    Notification.show("No products found in category '" + category + "' in store '" + targetStoreName + "'.");
                }
            } else {
                Notification.show(response != null ? response.getMessage() : "Category search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchProductsInStoreByName(Integer storeId, String storeName, String productName) {
        this.isStoreSpecificSearch = true;
        this.targetStoreId = storeId;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        // First, fetch the store details to get the correct store name
        try {
            String storeUrl = String.format("http://localhost:8080/api/store/viewStore/%d", storeId);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<StoreDTO>> storeResponse = restTemplate.exchange(
                storeUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<StoreDTO>>() {}
            );

            Response<StoreDTO> storeBody = storeResponse.getBody();
            if (storeBody != null && storeBody.isSuccess() && storeBody.getData() != null) {
                this.targetStoreName = storeBody.getData().getName();
            } else {
                this.targetStoreName = storeName; // Fallback to provided name
            }
        } catch (Exception e) {
            this.targetStoreName = storeName; // Fallback to provided name
        }

        String url = "http://localhost:8080/api/product/searchProducts/name/" + productName;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                
                List<ProductDTO> storeProducts = products.stream()
                    .filter(p -> p.getStoreIds().contains(storeId))
                    .collect(Collectors.toList());
                
                this.allProducts = storeProducts;
                this.storeProductData = fetchStoreProductData(storeProducts, token);
                this.storeData = fetchStoreData(storeProductData, token);

                List<FilteredProductResult> results = storeProducts.stream()
                    .map(p -> new FilteredProductResult(p, Set.of(storeId)))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
                
                if (results.isEmpty()) {
                    Notification.show("Product '" + productName + "' not found in store '" + targetStoreName + "'.");
                }
            } else {
                Notification.show(response != null ? response.getMessage() : "Product search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    // Original search methods for general search (unchanged)
    private void searchProductsByKeyword(String keyword) {
        this.isStoreSpecificSearch = false;
        this.targetStoreId = null;
        this.targetStoreName = null;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        String url = "http://localhost:8080/api/product/searchProducts/keyword/" + keyword;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                this.allProducts = products;

                // Fetch additional data for rendering columns
                this.storeProductData = fetchStoreProductData(products, token);
                this.storeData = fetchStoreData(storeProductData, token);

                // Convert to FilteredProductResult with all stores initially
                List<FilteredProductResult> results = products.stream()
                    .map(p -> new FilteredProductResult(p, new HashSet<>(p.getStoreIds())))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
            } else {
                Notification.show(response != null ? response.getMessage() : "Search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchProductsByCategory(String category) {
        this.isStoreSpecificSearch = false;
        this.targetStoreId = null;
        this.targetStoreName = null;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        String url = "http://localhost:8080/api/product/searchProducts/category/" + category;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                this.allProducts = products;

                // Fetch additional data for rendering columns
                this.storeProductData = fetchStoreProductData(products, token);
                this.storeData = fetchStoreData(storeProductData, token);

                // Convert to FilteredProductResult with all stores initially
                List<FilteredProductResult> results = products.stream()
                    .map(p -> new FilteredProductResult(p, new HashSet<>(p.getStoreIds())))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
            } else {
                Notification.show(response != null ? response.getMessage() : "Category search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchByProductName(String name) {
        this.isStoreSpecificSearch = false;
        this.targetStoreId = null;
        this.targetStoreName = null;
        
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            Notification.show("Session expired. Please log in again.");
            UI.getCurrent().navigate(HomeView.class);
            return;
        }
        String token = (String) session.getAttribute("token");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        String url = "http://localhost:8080/api/product/searchProducts/name/" + name;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Response<List<ProductDTO>>> apiResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductDTO>>>() {}
            );

            Response<List<ProductDTO>> response = apiResponse.getBody();
            if (response != null && response.isSuccess()) {
                List<ProductDTO> products = response.getData();
                this.allProducts = products;

                // Fetch additional data for rendering columns
                this.storeProductData = fetchStoreProductData(products, token);
                this.storeData = fetchStoreData(storeProductData, token);

                // Convert to FilteredProductResult with all stores initially
                List<FilteredProductResult> results = products.stream()
                    .map(p -> new FilteredProductResult(p, new HashSet<>(p.getStoreIds())))
                    .collect(Collectors.toList());

                grid.setItems(results);
                configureGrid();
            } else {
                Notification.show(response != null ? response.getMessage() : "Product search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    // Helper method to fetch StoreProductDTO data
    private List<StoreProductDTO> fetchStoreProductData(List<ProductDTO> products, String token) {
        List<StoreProductDTO> storeProducts = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (ProductDTO product : products) {
            Set<Integer> storeIds = isStoreSpecificSearch ? 
                Set.of(targetStoreId) : new HashSet<>(product.getStoreIds());
                
            for (Integer storeId : storeIds) {
                try {
                    String url = String.format("http://localhost:8080/api/product/getProductFromStore/%d/%d", storeId, product.getId());
                    ResponseEntity<Response<StoreProductDTO>> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<StoreProductDTO>>() {}
                    );
                    Response<StoreProductDTO> body = response.getBody();
                    if (body != null && body.isSuccess() && body.getData() != null) {
                        storeProducts.add(body.getData());
                    }
                } catch (Exception e) {
                    // Log error but continue with other products/stores
                    System.err.println("Error fetching store product data for product ID " + product.getId() + ": " + e.getMessage());
                }
            }
        }
        return storeProducts;
    }

    // Helper method to fetch StoreDTO data
    private Map<Integer, StoreDTO> fetchStoreData(List<StoreProductDTO> storeProducts, String token) {
        Map<Integer, StoreDTO> storeData = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Set<Integer> storeIds = storeProducts.stream()
            .map(StoreProductDTO::getStoreId)
            .collect(Collectors.toSet());

        for (Integer storeId : storeIds) {
            try {
                String url = String.format("http://localhost:8080/api/store/viewStore/%d", storeId);
                ResponseEntity<Response<StoreDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<StoreDTO>>() {}
                );
                Response<StoreDTO> body = response.getBody();
                if (body != null && body.isSuccess() && body.getData() != null) {
                    storeData.put(storeId, body.getData());
                }
            } catch (Exception e) {
                // Log error but continue with other stores
                System.err.println("Error fetching store data for store ID " + storeId + ": " + e.getMessage());
            }
        }
        return storeData;
    }

    // Updated getStoresSummaryComponent with clickable store names
    private Span getStoresSummaryComponent(FilteredProductResult result, List<StoreProductDTO> storeProducts, Map<Integer, StoreDTO> storeData) {
        Span container = new Span();
        List<Integer> storeIdsList = new ArrayList<>(result.getMatchingStoreIds());
        
        for (int i = 0; i < storeIdsList.size(); i++) {
            Integer storeId = storeIdsList.get(i);
            StoreDTO store = storeData.get(storeId);
            StoreProductDTO sp = storeProducts.stream()
                .filter(dto -> dto.getProductId() == result.getId() && dto.getStoreId() == storeId)
                .findFirst()
                .orElse(null);

            if (store != null && sp != null) {
                // Create clickable store name
                Anchor storeLink = new Anchor();
                storeLink.setText(store.getName());
                storeLink.getStyle().set("text-decoration", "none");
                storeLink.getStyle().set("color", "#1976d2"); // Blue color for links
                storeLink.getStyle().set("cursor", "pointer");
                
                // Add click listener to navigate to store page
                storeLink.getElement().addEventListener("click", e -> {
                    // Navigate to store page with store ID parameter
                    UI.getCurrent().navigate("store/" + storeId);
                });
                
                container.add(storeLink);
                
                // Add price and rating info
                Span priceInfo = new Span(String.format(" (₪%.2f, ", sp.getBasePrice()));
                container.add(priceInfo);
                
                // Add colored star rating
                container.add(createSimpleStarRating(store.getAverageRating()));
                
                Span closingParen = new Span(")");
                container.add(closingParen);
                
                // Add comma separator if not the last item
                if (i < storeIdsList.size() - 1) {
                    container.add(new Span(", "));
                }
            }
        }

        if (container.getChildren().count() == 0) {
            container.add(new Span("Not available"));
        }
        
        return container;
    }

    // Updated helper method for Price column - shows price with specific store names
    private String getPrice(FilteredProductResult result, List<StoreProductDTO> storeProducts) {
        List<StoreProductDTO> matchingProducts = storeProducts.stream()
            .filter(dto -> dto.getProductId() == result.getId() && 
                          result.getMatchingStoreIds().contains(dto.getStoreId()))
            .collect(Collectors.toList());
        
        if (matchingProducts.isEmpty()) return "N/A";
        
        if (matchingProducts.size() == 1) {
            StoreProductDTO product = matchingProducts.get(0);
            if (isStoreSpecificSearch) {
                // For store-specific search, just show the price without store name
                return String.format("₪%.2f", product.getBasePrice());
            } else {
                StoreDTO store = storeData.get(product.getStoreId());
                String storeName = store != null ? store.getName() : "Unknown Store";
                return String.format("₪%.2f (%s)", product.getBasePrice(), storeName);
            }
        } else {
            // Multiple stores - show each price with its store name
            List<String> priceStoreList = matchingProducts.stream()
                .map(sp -> {
                    if (isStoreSpecificSearch) {
                        return String.format("₪%.2f", sp.getBasePrice());
                    } else {
                        StoreDTO store = storeData.get(sp.getStoreId());
                        String storeName = store != null ? store.getName() : "Unknown";
                        return String.format("₪%.2f (%s)", sp.getBasePrice(), storeName);
                    }
                })
                .sorted() // Sort for consistent display
                .collect(Collectors.toList());
            
            return String.join(", ", priceStoreList);
        }
    }

    @Override
public void beforeEnter(BeforeEnterEvent event) {
    var queryParams = event.getLocation().getQueryParameters().getParameters();
    String type = queryParams.getOrDefault("type", List.of("keyword")).get(0);
    String term = queryParams.getOrDefault("term", List.of("")).get(0);
    String storeIdParam = queryParams.getOrDefault("storeId", List.of("")).get(0);

    if (term == null || term.isEmpty()) {
        Notification.show("No search term provided.");
        return;
    }

    // Check if this is a store-specific search
    if (storeIdParam != null && !storeIdParam.isEmpty()) {
        try {
            Integer storeId = Integer.parseInt(storeIdParam);
            // We'll let the search methods fetch the store name themselves
            String tempStoreName = "Store " + storeId; // Temporary name, will be updated in search methods
            
            switch (type.toLowerCase()) {
                case "category":
                    searchProductsInStoreByCategory(storeId, tempStoreName, term);
                    break;
                case "product_name":
                    searchProductsInStoreByName(storeId, tempStoreName, term);
                    break;
                case "keyword":
                default:
                    searchProductsInStoreByKeyword(storeId, tempStoreName, term);
                    break;
            }
        } catch (NumberFormatException e) {
            Notification.show("Invalid store ID provided.");
            return;
        }
    } else {
        // General search (original functionality)
        switch (type.toLowerCase()) {
            case "category":
                searchProductsByCategory(term);
                break;
            case "product_name":
                searchByProductName(term);
                break;
            case "keyword":
            default:
                searchProductsByKeyword(term);
                break;
        }
    }
}
}