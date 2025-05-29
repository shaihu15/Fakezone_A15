package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.DiscountConditionDTO;
import ApplicationLayer.DTO.DiscountRequestDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

@Route(value = "store/:storeId/discount", layout = MainLayout.class)
public class DiscountView extends VerticalLayout implements BeforeEnterObserver {
    
    private final String backendUrl;
    private final RestTemplate restTemplate;
    private int storeId;
    private UserDTO currentUser;
    private String token;
    private List<StoreProductDTO> storeProducts;
    
    // UI Components
    private H2 pageTitle;
    private ComboBox<String> discountTypeCombo;
    private ComboBox<String> scopeCombo;
    private MultiSelectComboBox<StoreProductDTO> productSelector;
    private NumberField percentageField;
    private VerticalLayout conditionsSection;
    private List<ConditionRow> conditionRows;
    private Button addConditionButton;
    private Button createDiscountButton;
    private Button cancelButton;
    
    // Condition types mapping: User-friendly display name -> Backend API name
    private static final Map<String, String> CONDITION_TYPE_MAPPING = Map.of(
        "Contains specific product", "CONTAINS_PRODUCT",
        "Minimum product count", "PRODUCT_COUNT_ABOVE", 
        "Always apply", "ALWAYS_TRUE"
    );
    
    // Get display names for UI
    private static final String[] CONDITION_DISPLAY_NAMES = 
        CONDITION_TYPE_MAPPING.keySet().toArray(new String[0]);
    
    public DiscountView(@Value("${api.url}") String backendUrl) {
        this.backendUrl = backendUrl;
        this.restTemplate = new RestTemplate();
        this.restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        this.storeProducts = new ArrayList<>();
        this.conditionRows = new ArrayList<>();
        
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        pageTitle = new H2("Add Discount");
        
        // Discount Type ComboBox
        discountTypeCombo = new ComboBox<>("Discount Type");
        discountTypeCombo.setItems("SIMPLE", "CONDITION");
        discountTypeCombo.setValue("SIMPLE");
        discountTypeCombo.setRequired(true);
        discountTypeCombo.setWidthFull();
        
        // Scope ComboBox
        scopeCombo = new ComboBox<>("Discount Scope");
        scopeCombo.setItems("STORE", "PRODUCTS");
        scopeCombo.setPlaceholder("Select scope");
        scopeCombo.setRequired(true);
        scopeCombo.setWidthFull();
        
        // Product Selector (MultiSelect ComboBox)
        productSelector = new MultiSelectComboBox<>("Select Products");
        productSelector.setItemLabelGenerator(product -> 
            String.format("%s - $%.2f", product.getName(), product.getBasePrice()));
        productSelector.setPlaceholder("Choose products for discount");
        productSelector.setWidthFull();
        productSelector.setVisible(false); // Initially hidden
        
        // Percentage Field
        percentageField = new NumberField("Discount Percentage");
        percentageField.setPlaceholder("Enter percentage (0-100)");
        percentageField.setMin(0);
        percentageField.setMax(100);
        percentageField.setStep(0.1);
        percentageField.setSuffixComponent(new Span("%"));
        percentageField.setRequired(true);
        percentageField.setWidthFull();
        
        // Conditions Section
        conditionsSection = new VerticalLayout();
        conditionsSection.setPadding(false);
        conditionsSection.setSpacing(true);
        conditionsSection.setVisible(false); // Initially hidden
        
        H3 conditionsTitle = new H3("Discount Conditions");
        conditionsTitle.getStyle().set("margin-bottom", "10px").set("color", "#1976D2");
        
        addConditionButton = new Button("Add Condition", VaadinIcon.PLUS.create());
        addConditionButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addConditionButton.getStyle().set("background-color", "#4CAF50").set("color", "white");
        addConditionButton.addClickListener(e -> addConditionRow());
        
        conditionsSection.add(conditionsTitle, addConditionButton);
        
        // Event listeners for dynamic UI updates
        discountTypeCombo.addValueChangeListener(event -> {
            boolean isConditionDiscount = "CONDITION".equals(event.getValue());
            conditionsSection.setVisible(isConditionDiscount);
            if (isConditionDiscount && conditionRows.isEmpty()) {
                addConditionRow(); // Add first condition row automatically
            } else if (!isConditionDiscount) {
                clearConditions();
            }
            updatePageTitle();
        });
        
        scopeCombo.addValueChangeListener(event -> {
            boolean isProductScope = "PRODUCTS".equals(event.getValue());
            productSelector.setVisible(isProductScope);
            productSelector.setRequired(isProductScope);
            if (!isProductScope) {
                productSelector.clear();
            }
        });
        
        // Buttons
        createDiscountButton = new Button("Create Discount");
        createDiscountButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createDiscountButton.addClickListener(e -> createDiscount());
        
        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> navigateBack());
    }
    
    private void updatePageTitle() {
        String discountType = discountTypeCombo.getValue();
        pageTitle.setText("Add " + (discountType != null ? discountType : "") + " Discount");
    }
    
    private void addConditionRow() {
        ConditionRow row = new ConditionRow();
        conditionRows.add(row);
        conditionsSection.add(row.getLayout());
    }
    
    private void removeConditionRow(ConditionRow row) {
        conditionRows.remove(row);
        conditionsSection.remove(row.getLayout());
    }
    
    private void clearConditions() {
        conditionRows.clear();
        // Remove all condition row layouts but keep title and add button
        conditionsSection.removeAll();
        H3 conditionsTitle = new H3("Discount Conditions");
        conditionsTitle.getStyle().set("margin-bottom", "10px").set("color", "#1976D2");
        conditionsSection.add(conditionsTitle, addConditionButton);
    }
    
    // Inner class for condition rows
    private class ConditionRow {
        private HorizontalLayout layout;
        private ComboBox<String> typeCombo;
        private NumberField thresholdField;
        private ComboBox<StoreProductDTO> productCombo;
        private Button removeButton;
        
        public ConditionRow() {
            createLayout();
        }
        
        private void createLayout() {
            layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.END);
            layout.setSpacing(true);
            layout.setWidthFull();
            layout.getStyle().set("background-color", "#F8F9FA")
                            .set("border-radius", "8px")
                            .set("padding", "10px")
                            .set("margin-bottom", "5px");
            
            // Condition Type Selector
            typeCombo = new ComboBox<>("Condition Type");
            typeCombo.setItems(CONDITION_DISPLAY_NAMES);
            typeCombo.setPlaceholder("Select condition");
            typeCombo.setRequired(true);
            typeCombo.setWidth("200px");
            
            // Threshold Field (for numeric conditions)
            thresholdField = new NumberField("Threshold");
            thresholdField.setPlaceholder("Enter value");
            thresholdField.setMin(0);
            thresholdField.setVisible(false);
            thresholdField.setWidth("150px");
            
            // Product Selector (for product-based conditions)
            productCombo = new ComboBox<>("Product");
            productCombo.setItems(storeProducts);
            productCombo.setItemLabelGenerator(product -> 
                String.format("%s - $%.2f", product.getName(), product.getBasePrice()));
            productCombo.setPlaceholder("Select product");
            productCombo.setVisible(false);
            productCombo.setWidth("250px");
            
            // Remove Button
            removeButton = new Button(VaadinIcon.MINUS.create());
            removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            removeButton.getStyle().set("background-color", "#F44336");
            removeButton.addClickListener(e -> removeConditionRow(this));
            
            // Event listener for condition type changes
            typeCombo.addValueChangeListener(event -> {
                String selectedType = event.getValue();
                updateFieldsVisibility(selectedType);
            });
            
            layout.add(typeCombo, thresholdField, productCombo, removeButton);
        }
        
        private void updateFieldsVisibility(String conditionType) {
            // Hide all fields first
            thresholdField.setVisible(false);
            productCombo.setVisible(false);
            thresholdField.setRequired(false);
            productCombo.setRequired(false);
            
            if (conditionType == null) return;
            
            switch (conditionType) {
                case "Contains specific product":
                    productCombo.setVisible(true);
                    productCombo.setRequired(true);
                    break;
                case "Minimum product count":
                    thresholdField.setVisible(true);
                    thresholdField.setRequired(true);
                    thresholdField.setLabel("Minimum Product Count");
                    break;
                case "Always apply":
                    // No additional fields needed
                    break;
            }
        }
        
        public HorizontalLayout getLayout() {
            return layout;
        }
        
        public DiscountConditionDTO toDTO() {
            DiscountConditionDTO dto = new DiscountConditionDTO();
            dto.setType(CONDITION_TYPE_MAPPING.get(typeCombo.getValue()));
            
            if (thresholdField.isVisible() && thresholdField.getValue() != null) {
                dto.setThreshold(thresholdField.getValue());
            }
            
            if (productCombo.isVisible() && productCombo.getValue() != null) {
                dto.setProductId(productCombo.getValue().getProductId());
            }
            
            return dto;
        }
        
        public boolean isValid() {
            if (typeCombo.getValue() == null) {
                return false;
            }
            
            String type = typeCombo.getValue();
            switch (type) {
                case "Minimum product count":
                    return thresholdField.getValue() != null && thresholdField.getValue() > 0;
                case "Contains specific product":
                    return productCombo.getValue() != null;
                case "Always apply":
                    return true;
                default:
                    return false;
            }
        }
    }
    
    private void setupLayout() {
        // Form Layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(
            discountTypeCombo,
            scopeCombo,
            productSelector,
            percentageField
        );
        
        // Button Layout
        HorizontalLayout buttonLayout = new HorizontalLayout(createDiscountButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);
        
        // Back Link
        RouterLink backLink = new RouterLink("← Back to Store Management", 
            StoreManageView.class, new RouteParameters("storeId", String.valueOf(storeId)));
        backLink.getStyle().set("margin-bottom", "20px").set("color", "#1976D2");
        
        // Main Card
        VerticalLayout card = createCard();
        card.add(pageTitle, formLayout, conditionsSection, buttonLayout);
        
        add(backLink, card);
    }
    
    private VerticalLayout createCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.setMaxWidth("800px"); // Increased width to accommodate conditions
        card.getStyle()
            .set("border", "1px solid #E0E0E0")
            .set("border-radius", "8px")
            .set("background-color", "#FFFFFF")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        card.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        return card;
    }
    
    private void fetchStoreProducts() {
        try {
            String url = String.format("%sstore/viewStore/%d", backendUrl, storeId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Response<StoreDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<StoreDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                StoreDTO store = response.getBody().getData();
                storeProducts = new ArrayList<>(store.getStoreProducts());
                productSelector.setItems(storeProducts);
                
                // Update product combos in existing condition rows
                for (ConditionRow row : conditionRows) {
                    row.productCombo.setItems(storeProducts);
                }
            } else {
                showErrorNotification("Failed to load store products: " + 
                    (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
            }
        } catch (Exception e) {
            showErrorNotification("Error fetching store products: " + e.getMessage());
        }
    }
    
    private void createDiscount() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Create request DTO
            DiscountRequestDTO discountRequest = new DiscountRequestDTO();
            discountRequest.setDiscountType(discountTypeCombo.getValue());
            discountRequest.setScope(scopeCombo.getValue());
            discountRequest.setPercentage(percentageField.getValue());
            
            // Handle product selection for PRODUCTS scope
            if ("PRODUCTS".equals(scopeCombo.getValue())) {
                Set<StoreProductDTO> selectedProducts = productSelector.getSelectedItems();
                List<Integer> productIds = selectedProducts.stream()
                    .map(StoreProductDTO::getProductId)
                    .collect(Collectors.toList());
                discountRequest.setProductIds(productIds);
            }
            
            // Handle conditions for CONDITION discount type
            if ("CONDITION".equals(discountTypeCombo.getValue())) {
                List<DiscountConditionDTO> conditions = conditionRows.stream()
                    .map(ConditionRow::toDTO)
                    .collect(Collectors.toList());
                discountRequest.setConditions(conditions);
            }
            
            // Make API call
            String url = String.format("%sdiscount/add/%d/%d", backendUrl, storeId, currentUser.getUserId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            
            HttpEntity<DiscountRequestDTO> entity = new HttpEntity<>(discountRequest, headers);
            
            ResponseEntity<Response<Void>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Response<Void>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isSuccess()) {
                showSuccessNotification("Discount created successfully!");
                navigateBack();
            } else {
                String errorMessage = response.getBody() != null ? 
                    response.getBody().getMessage() : "Unknown error occurred";
                showErrorNotification("Failed to create discount: " + errorMessage);
            }
            
        } catch (Exception e) {
            showErrorNotification("Error creating discount: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        if (discountTypeCombo.getValue() == null) {
            showErrorNotification("Please select a discount type");
            discountTypeCombo.focus();
            return false;
        }
        
        if (scopeCombo.getValue() == null) {
            showErrorNotification("Please select a discount scope");
            scopeCombo.focus();
            return false;
        }
        
        if (percentageField.getValue() == null) {
            showErrorNotification("Please enter a discount percentage");
            percentageField.focus();
            return false;
        }
        
        if (percentageField.getValue() <= 0 || percentageField.getValue() > 100) {
            showErrorNotification("Percentage must be between 0 and 100");
            percentageField.focus();
            return false;
        }
        
        // Validate product selection for PRODUCTS scope
        if ("PRODUCTS".equals(scopeCombo.getValue())) {
            if (productSelector.getSelectedItems().isEmpty()) {
                showErrorNotification("Please select at least one product for the discount");
                productSelector.focus();
                return false;
            }
        }
        
        // Validate conditions for CONDITION discount type
        if ("CONDITION".equals(discountTypeCombo.getValue())) {
            if (conditionRows.isEmpty()) {
                showErrorNotification("Please add at least one condition for the discount");
                return false;
            }
            
            for (int i = 0; i < conditionRows.size(); i++) {
                ConditionRow row = conditionRows.get(i);
                if (!row.isValid()) {
                    showErrorNotification("Please fill in all required fields for condition " + (i + 1));
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    private void showInfoDialog(String title, String message) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        
        VerticalLayout content = new VerticalLayout();
        content.add(new Span(message));
        content.setPadding(false);
        content.setSpacing(true);
        
        Button okButton = new Button("OK", e -> dialog.close());
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(okButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        
        content.add(buttonLayout);
        dialog.add(content);
        dialog.open();
    }
    
    private void navigateBack() {
        getUI().ifPresent(ui -> ui.navigate(StoreManageView.class, 
            new RouteParameters("storeId", String.valueOf(storeId))));
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get store ID from URL
        try {
            storeId = Integer.parseInt(event.getRouteParameters().get("storeId").orElse("0"));
            if (storeId == 0) {
                event.forwardTo("user");
                return;
            }
        } catch (NumberFormatException e) {
            event.forwardTo("user");
            return;
        }
        
        // Get user session data
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            event.forwardTo("login");
            return;
        }
        
        token = (String) session.getAttribute("token");
        currentUser = (UserDTO) session.getAttribute("userDTO");
        
        if (token == null || currentUser == null) {
            event.forwardTo("login");
            return;
        }
        
        // Fetch store products for the product selector
        fetchStoreProducts();
        
        // Update the back link with correct store ID
        updateBackLink();
    }
    
    private void updateBackLink() {
        // Remove existing components and re-add with updated store ID
        removeAll();
        setupLayout();
    }
} 