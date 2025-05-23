package com.fakezone.fakezone.ui.view;


import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.RequestDataTypes.LoginRequest;
import ApplicationLayer.RequestDataTypes.RegisterUserRequest;
import ApplicationLayer.DTO.UserDTO;
import InfrastructureLayer.Security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class MainLayout extends AppLayout implements RouterLayout {
    RestTemplate restTemplate;
    private final String apiUrl;
    private final String webUrl;
    public MainLayout(@Value("${api.url}") String apiUrl, @Value("${website.url}") String webUrl) {
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        initSession();
        createHeader();
    }

    private void initSession() {
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpServletResponse response = (HttpServletResponse) VaadinResponse.getCurrent();
        HttpSession session = request.getSession(true); // true = create if not exist

        String token = (String) session.getAttribute("token");
        if (token == null) {
            String url = "http://localhost:8080/api/user/generateGuestToken";
            try{
                ResponseEntity<Response> apiResponse = restTemplate.getForEntity(url, Response.class);
                Response<String> tokenResponse = (Response<String>) apiResponse.getBody();
                if(tokenResponse != null && tokenResponse.isSuccess()){
                    session.setAttribute("token", tokenResponse.getData());
                    jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", token);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(60 * 60 * 5); // 5 hours
                    response.addCookie(cookie);
                }
                else{
                    Notification.show(apiResponse.getBody().getMessage());
                }
            }
            catch(Exception e){
                Notification.show(e.getMessage());
            }

            
        }
    }

    private void createHeader() {
        // LOGO
        Image logo = new Image("images/fakezon_logo.jpeg", "HomePage");
        logo.setHeight("50px");
        Anchor logoAnchor = new Anchor("/", logo);
        logoAnchor.getElement().setAttribute("router-ignore", true);

        // SPACER - no longer needed
        Div spacer = new Div();
        spacer.setWidth("20%");

         // SEARCH BAR
        HorizontalLayout searchLayout = new HorizontalLayout();
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search Everywhere...");
        // search type selecting
        ComboBox<String> searchType = new ComboBox<>();
        searchType.setPlaceholder("Search by...");
        searchType.setItems("Keyword", "Category", "Product Name");
        searchType.setValue("Keyword"); // Default value
        Button searchButton = new Button(new Icon(VaadinIcon.SEARCH));
        searchButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        searchLayout.add(searchType, searchField, searchButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchButton.addClickListener(event -> performSearch(searchField.getValue(),searchType.getValue())); // Search button
        searchField.addKeyDownListener(Key.ENTER, event -> performSearch(searchField.getValue(),searchType.getValue())); // Enter key
        searchLayout.setFlexGrow(0.4, searchField);

        // LOGIN/REGISTER BUTTON
        Button loginRegisterLogoutButton = null;
        Button notificationsButton = null;
        if(isGuestToken()){
            loginRegisterLogoutButton = new Button("Login/Register");
            loginRegisterLogoutButton.addClickListener(event -> loginRegisterClick());
        }
        else{
            loginRegisterLogoutButton = new Button("Logout");
            loginRegisterLogoutButton.addClickListener(event -> logoutClick());
        
            //NOTIFS
            Icon notificationsIcon = VaadinIcon.BELL.create();
            notificationsIcon.setSize("30px");
            notificationsButton = new Button(notificationsIcon);
            notificationsButton.addClickListener(event -> showNotifications());
        }

        // CART
        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.setSize("30px");
        Button cartButton = new Button(cartIcon);
        cartButton.addClickListener(event -> showCart());

        
        // HEADER LAYOUT
        HorizontalLayout header = new HorizontalLayout(logoAnchor, spacer, searchLayout);
        if(notificationsButton != null){
            header.add(notificationsButton);
        }
        header.add(loginRegisterLogoutButton, cartButton);
        header.setWidth("100%");
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.getStyle().set("background", "#ffffff").set("padding", "10px");
        header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, searchLayout);
        addToNavbar(header);
    }

    private boolean isGuestToken(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        String url = "http://localhost:8080/api/user/isGuestToken";
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


    private void openRegisterDialog() {
        Dialog registerDialog = new Dialog();
        registerDialog.setHeaderTitle("Register");

        FormLayout registerFormLayout = new FormLayout();
        EmailField registerEmailField = new EmailField("Email");
        PasswordField registerPasswordField = new PasswordField("Password");
        PasswordField registerConfirmPasswordField = new PasswordField("Confirm Password");
        TextField dobField = new TextField("Date of Birth");
        registerEmailField.setValueChangeMode(ValueChangeMode.EAGER);
        registerPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        registerConfirmPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        dobField.setValueChangeMode(ValueChangeMode.EAGER);
        dobField.setPlaceholder("YYYY-MM-DD");
        registerPasswordField.getElement().setProperty("helperText", "Password must contain letters and numbers, and be at least 6 characters long.");
        registerPasswordField.setHelperText("Password must contain letters and numbers, and be at least 6 characters long.");
        ComboBox<String> countryComboBox = new ComboBox<>("Country");
        List<String> countryNames = Arrays.stream(Locale.getISOCountries())
            .map(code -> Locale.forLanguageTag("und-" + code).getDisplayCountry())
            .sorted()
            .collect(Collectors.toList());
        countryComboBox.setItems(countryNames);
        registerFormLayout.add(registerEmailField, registerPasswordField, registerConfirmPasswordField, dobField, countryComboBox);

        Button registerButton = new Button("Register", e -> {
            register(registerEmailField, registerPasswordField, dobField, countryComboBox, registerDialog);
        });
        registerButton.setEnabled(false);
        registerEmailField.addValueChangeListener(event -> validateRegisterForm(registerEmailField, registerPasswordField, 
                                                                    registerConfirmPasswordField, registerButton, dobField, countryComboBox));
        registerPasswordField.addValueChangeListener(event -> validateRegisterForm(registerEmailField, registerPasswordField, 
                                                                    registerConfirmPasswordField, registerButton, dobField, countryComboBox));
        registerConfirmPasswordField.addValueChangeListener(event -> validateRegisterForm(registerEmailField, registerPasswordField, 
                                                                    registerConfirmPasswordField, registerButton, dobField, countryComboBox));
        dobField.addValueChangeListener(event -> validateRegisterForm(registerEmailField, registerPasswordField, 
                                                                    registerConfirmPasswordField, registerButton, dobField, countryComboBox));
        countryComboBox.addValueChangeListener(event -> validateRegisterForm(registerEmailField, registerPasswordField, 
                                                                    registerConfirmPasswordField, registerButton, dobField, countryComboBox));

        registerEmailField.addKeyDownListener(Key.ENTER, event ->  {if (registerButton.isEnabled())  registerButton.click();;});
        registerPasswordField.addKeyDownListener(Key.ENTER, event -> {if (registerButton.isEnabled())  registerButton.click();;});
        registerConfirmPasswordField.addKeyDownListener(Key.ENTER, event -> {if (registerButton.isEnabled())  registerButton.click();;});
        dobField.addKeyDownListener(Key.ENTER, event -> {if (registerButton.isEnabled())  registerButton.click();;});

        registerDialog.add(registerFormLayout);
        registerDialog.getFooter().add(registerButton);
        registerDialog.open();
    }

    private boolean validateRegisterForm(EmailField emailField, PasswordField passwordField, PasswordField confirmPasswordField, Button registerButton, TextField dobField, ComboBox<String> countryComboBox) {
        boolean isEmailValid = emailField.isInvalid() == false && !emailField.getValue().isEmpty();
        boolean isPasswordValid = passwordField.getValue() != null && !passwordField.getValue().isEmpty();
        boolean arePasswordsEqual = isPasswordValid && confirmPasswordField.getValue() != null && passwordField.getValue().equals(confirmPasswordField.getValue());
        boolean isPasswordComplexEnough = isPasswordValid && passwordField.getValue().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
        boolean isValidDate = isValidDate(dobField.getValue());
        String selectedCountryName = countryComboBox.getValue();
        String countryCode = getCountryCodeFromName(selectedCountryName);
        boolean isCountrySelected = countryCode != "";
        registerButton.setEnabled(isEmailValid && isPasswordValid && arePasswordsEqual && isPasswordComplexEnough && isValidDate && isCountrySelected);
        return isEmailValid && isPasswordValid && arePasswordsEqual && isPasswordComplexEnough && isValidDate && isCountrySelected;
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

    private boolean isValidDate(String date) {
        if(date == null)
            return false;
        String datePattern = "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
        return date.matches(datePattern);
    }

    private void register(EmailField registerEmailField, PasswordField registerPasswordField, TextField dobField, ComboBox<String> countryComboBox, Dialog registerDialog){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        RegisterUserRequest regReq = new RegisterUserRequest(registerEmailField.getValue(), registerPasswordField.getValue(), dobField.getValue(), getCountryCodeFromName(countryComboBox.getValue()));
        Request<RegisterUserRequest> req = new Request<RegisterUserRequest>(token, regReq);
        String url = "http://localhost:8080/api/user/register";
    
        ResponseEntity<Response<String>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(req),
            new ParameterizedTypeReference<Response<String>>() {}
        );
        Response<String> response = (Response<String>) apiResponse.getBody();
        if(response.isSuccess()){
            Notification.show("Registered succefully");
        }
        else{
            Notification.show(response.getMessage());
        }
    
        registerDialog.close();

    }

    private void testDialog(){
        Dialog testDialog = new Dialog();
        testDialog.setHeaderTitle("NOT IMPLEMENTED YET");
        testDialog.open();
    }

    // private void performSearch(String searchText){
    //     //TO DO - RN JUST FOR TEST
    //     if (searchText != null && !searchText.isEmpty()) {
    //         // Perform the search operation with searchTerm
    //         Notification.show("Searching for: " + searchText);  // Replace with your actual search logic
    //     } else {
    //         Notification.show("Please enter a search term.");
    //     }
       
    // }

    private void performSearch(String searchText, String type) {
            if (searchText == null || searchText.trim().isEmpty()) {
                Notification.show("Please enter a search term.");
                return;
            }
              String trimmed = searchText.trim();
            String queryType;
            switch (type.toLowerCase()) {
                case "category":
                    queryType = "category";
                    break;
                case "product name":
                    queryType = "product_name";
                    break;
                case "keyword":
                default:
                    queryType = "keyword";
                    break;
            }
            UI.getCurrent().navigate("search",
                new QueryParameters(
                    Map.of(
                        "type", List.of(queryType),
                        "term", List.of(trimmed)
                    )
                )
            );
        }
    private void showNotifications(){
        testDialog();
    }


    private void showCart(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        if(user != null){
            UI.getCurrent().navigate(webUrl+"cart/" + user.getUserId());
        }
        else{
            testDialog();
        }
    }

    private void loginRegisterClick(){
        Dialog loginDialog = new Dialog();
        loginDialog.setHeaderTitle("Login");
        FormLayout loginFormLayout = new FormLayout();
        EmailField loginEmailField = new EmailField("Email");
        PasswordField loginPasswordField = new PasswordField("Password");
        loginEmailField.setValueChangeMode(ValueChangeMode.EAGER);
        loginPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
        Button loginButton = new Button("Login");
        loginButton.setEnabled(false);
        loginButton.addClickListener(e -> login(loginEmailField, loginPasswordField, loginDialog));
        loginEmailField.addKeyDownListener(Key.ENTER, e -> {if(loginButton.isEnabled()) loginButton.click();});
        loginPasswordField.addKeyDownListener(Key.ENTER, e -> {if(loginButton.isEnabled()) loginButton.click();});
        loginEmailField.addValueChangeListener(event -> validateLoginButton(loginEmailField, loginPasswordField, loginButton));
        loginPasswordField.addValueChangeListener(event -> validateLoginButton(loginEmailField, loginPasswordField, loginButton));
        Button registerButton = new Button("Register", e -> {
            loginDialog.close(); // Close the Login Dialog
            openRegisterDialog(); // Open the Register Dialog
        });

        loginFormLayout.add(loginEmailField, loginPasswordField);
        HorizontalLayout loginButtonsLayout = new HorizontalLayout(loginButton, registerButton);
        loginDialog.add(loginFormLayout, loginButtonsLayout);
        loginDialog.open(); // Open the Login Dialog
    }

    private boolean validateLoginButton(EmailField loginEmailField,PasswordField loginPasswordField, Button loginButton){
        boolean validEmail = !loginEmailField.isInvalid() && !loginEmailField.getValue().isEmpty();
        boolean validPass = loginPasswordField.getValue() != null && !loginPasswordField.getValue().isEmpty();
        loginButton.setEnabled(validPass && validEmail);
        return validPass && validEmail;
    }

    private void login(EmailField loginEmailField,PasswordField loginPasswordField, Dialog loginDialog){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        LoginRequest logReq = new LoginRequest(loginEmailField.getValue(), loginPasswordField.getValue());
        Request<LoginRequest> req = new Request<LoginRequest>(token, logReq);
        String url = "http://localhost:8080/api/user/login";
        ResponseEntity<Response<UserDTO>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(req),
            new ParameterizedTypeReference<Response<UserDTO>>() {}
        );            
        Response<UserDTO> response = apiResponse.getBody();
        if(response.isSuccess()){
            session.setAttribute("token", response.getToken());
            session.setAttribute("userDTO", response.getData());
            UI.getCurrent().getPage().reload();
        }
        else{
            loginDialog.add(new Paragraph(response.getMessage()));
        }
    }
    
    private void logoutClick(){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        Request<Integer> req = new Request<Integer>(token, ((UserDTO) session.getAttribute("userDTO")).getUserId());
        String url = "http://localhost:8080/api/user/logout";
        ResponseEntity<Response<Void>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(req),
            new ParameterizedTypeReference<Response<Void>>() {}
        );            
        Response<Void> response = apiResponse.getBody();
        if(response.isSuccess()){
            session.removeAttribute("token");
            session.removeAttribute("userDTO");
            UI.getCurrent().getPage().reload();
        }
        else{
            Notification.show(response.getMessage());
        }
        
    }


}
