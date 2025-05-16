package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
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

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.vaadin.flow.component.grid.Grid;

@Route(value = "search", layout = MainLayout.class)
public class SearchResultsView extends VerticalLayout implements BeforeEnterObserver {
    private Grid<ProductDTO> grid;
    private List<ProductDTO> allProducts;

    public SearchResultsView() {
        grid = new Grid<>(ProductDTO.class);
        add(grid);
        Button filterButton = new Button(VaadinIcon.FILTER.create());
        filterButton.addClickListener(e -> openFilterDialog());
        add(filterButton);
    }

    private void openFilterDialog() {
        Dialog filterDialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        filterDialog.open();

        NumberField minPrice = new NumberField("Min Price");
        NumberField maxPrice = new NumberField("Max Price");

        ComboBox<Integer> productRating = new ComboBox<>("Product Rating", List.of(1, 2, 3, 4, 5));
        productRating.setItemLabelGenerator(r -> "★".repeat(r) + "☆".repeat(5 - r));

        ComboBox<Integer> storeRating = new ComboBox<>("Store Rating", List.of(1, 2, 3, 4, 5));
        storeRating.setItemLabelGenerator(r -> "★".repeat(r) + "☆".repeat(5 - r));

        ComboBox<String> category = new ComboBox<>("Category");
        category.setItems(Arrays.stream(PCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList()));

        Button apply = new Button("Apply Filters", e -> {
            if (allProducts == null) {
                Notification.show("No products to filter.");
                return;
            }

            Double min = minPrice.getValue();
            Double max = maxPrice.getValue();
            Integer selectedProductRating = productRating.getValue();
            Integer selectedStoreRating = storeRating.getValue();
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

            List<ProductDTO> filtered = allProducts.stream()
                .filter(product -> {
                    Set<Integer> matchingStores = product.getStoresIds().stream()
                        .filter(storeId -> {
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

                                boolean priceOk = (min == null || dto.getBasePrice() >= min) &&
                                                (max == null || dto.getBasePrice() <= max);

                                boolean productRatingOk = selectedProductRating == null ||
                                                        Math.round(dto.getAverageRating()) == selectedProductRating;

                                boolean storeRatingOk = selectedStoreRating == null ||
                                                        Math.round(storeDTO.getAverageRating()) == selectedStoreRating;

                                boolean categoryOk = selectedCategory == null ||
                                                    dto.getCategory().name().equalsIgnoreCase(selectedCategory);

                                return priceOk && productRatingOk && storeRatingOk && categoryOk;

                            } catch (Exception ex) {
                                return false;
                            }
                        }).collect(Collectors.toSet());

                    return !matchingStores.isEmpty();
                })
                .collect(Collectors.toList());

            grid.setItems(filtered);
            filterDialog.close();
        });

        layout.add(minPrice, maxPrice, productRating, storeRating, category, apply);
        filterDialog.add(layout);
    }

    private void searchProductsByKeyword(String keyword) {
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
                List<StoreProductDTO> storeProductData = fetchStoreProductData(products, token);
                Map<Integer, StoreDTO> storeData = fetchStoreData(storeProductData, token);

                grid.removeAllColumns();
                grid.setItems(products);
                grid.addColumn(ProductDTO::getName).setHeader("Name");
                grid.addColumn(p -> p.getCategory().name()).setHeader("Category");
                grid.addColumn(ProductDTO::getDescription).setHeader("Description");
                grid.addColumn(p -> getStoresSummary(p, storeProductData, storeData)).setHeader("Available in Stores");
                grid.addColumn(p -> getPrice(p, storeProductData)).setHeader("Price");
                grid.addColumn(p -> getProductRating(p, storeProductData)).setHeader("Product Rating");
                grid.addColumn(p -> getStoreRating(p, storeProductData, storeData)).setHeader("Store Rating");
            } else {
                Notification.show(response != null ? response.getMessage() : "Search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchProductsByCategory(String category) {
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
                List<StoreProductDTO> storeProductData = fetchStoreProductData(products, token);
                Map<Integer, StoreDTO> storeData = fetchStoreData(storeProductData, token);

                grid.removeAllColumns();
                grid.setItems(products);
                grid.addColumn(ProductDTO::getName).setHeader("Name");
                grid.addColumn(p -> p.getCategory().name()).setHeader("Category");
                grid.addColumn(ProductDTO::getDescription).setHeader("Description");
                grid.addColumn(p -> getStoresSummary(p, storeProductData, storeData)).setHeader("Available in Stores");
                grid.addColumn(p -> getPrice(p, storeProductData)).setHeader("Price");
                grid.addColumn(p -> getProductRating(p, storeProductData)).setHeader("Product Rating");
                grid.addColumn(p -> getStoreRating(p, storeProductData, storeData)).setHeader("Store Rating");
            } else {
                Notification.show(response != null ? response.getMessage() : "Category search failed.");
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void searchByProductName(String name) {
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
                List<StoreProductDTO> storeProductData = fetchStoreProductData(products, token);
                Map<Integer, StoreDTO> storeData = fetchStoreData(storeProductData, token);

                grid.removeAllColumns();
                grid.setItems(products);
                grid.addColumn(ProductDTO::getName).setHeader("Name");
                grid.addColumn(p -> p.getCategory().name()).setHeader("Category");
                grid.addColumn(ProductDTO::getDescription).setHeader("Description");
                grid.addColumn(p -> getStoresSummary(p, storeProductData, storeData)).setHeader("Available in Stores");
                grid.addColumn(p -> getPrice(p, storeProductData)).setHeader("Price");
                grid.addColumn(p -> getProductRating(p, storeProductData)).setHeader("Product Rating");
                grid.addColumn(p -> getStoreRating(p, storeProductData, storeData)).setHeader("Store Rating");
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
            for (Integer storeId : product.getStoresIds()) {
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
                    Notification.show("Error fetching store product data for product ID " + product.getId() + ": " + e.getMessage());
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
                Notification.show("Error fetching store data for store ID " + storeId + ": " + e.getMessage());
            }
        }
        return storeData;
    }

    // Updated getStoresSummary to use pre-fetched data
    private String getStoresSummary(ProductDTO product, List<StoreProductDTO> storeProducts, Map<Integer, StoreDTO> storeData) {
        StringBuilder sb = new StringBuilder();
        for (Integer storeId : product.getStoresIds()) {
            StoreDTO store = storeData.get(storeId);
            StoreProductDTO sp = storeProducts.stream()
                .filter(dto -> dto.getProductId() == product.getId() && dto.getStoreId() == storeId)
                .findFirst()
                .orElse(null);

            if (store != null && sp != null) {
                sb.append(String.format("%s (₪%.2f, ⭐%.1f), ", store.getName(), sp.getBasePrice(), store.getAverageRating()));
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // Remove trailing comma
            return sb.toString();
        }
        return "Not available";
    }

    // Helper method for Price column
    private String getPrice(ProductDTO product, List<StoreProductDTO> storeProducts) {
        Optional<StoreProductDTO> sp = storeProducts.stream()
            .filter(dto -> dto.getProductId() == product.getId())
            .findFirst();
        return sp.map(dto -> String.format("₪%.2f", dto.getBasePrice())).orElse("N/A");
    }

    // Helper method for Product Rating column
    private String getProductRating(ProductDTO product, List<StoreProductDTO> storeProducts) {
        Optional<StoreProductDTO> sp = storeProducts.stream()
            .filter(dto -> dto.getProductId() == product.getId())
            .findFirst();
        return sp.map(dto -> String.format("%.1f", dto.getAverageRating())).orElse("N/A");
    }

    // Helper method for Store Rating column
    private String getStoreRating(ProductDTO product, List<StoreProductDTO> storeProducts, Map<Integer, StoreDTO> storeData) {
        List<Integer> storeIds = new ArrayList<>(product.getStoresIds());
        if (storeIds.isEmpty()) return "N/A";
        Optional<Double> rating = storeIds.stream()
            .map(storeData::get)
            .filter(Objects::nonNull)
            .map(StoreDTO::getAverageRating)
            .findFirst();
        return rating.map(r -> String.format("%.1f", r)).orElse("N/A");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var queryParams = event.getLocation().getQueryParameters().getParameters();
        String type = queryParams.getOrDefault("type", List.of("keyword")).get(0);
        String term = queryParams.getOrDefault("term", List.of("")).get(0);

        if (term == null || term.isEmpty()) {
            Notification.show("No search term provided.");
            return;
        }

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