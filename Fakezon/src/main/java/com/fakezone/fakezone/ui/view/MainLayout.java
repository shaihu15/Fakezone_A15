package com.fakezone.fakezone.ui.view;


import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class MainLayout extends AppLayout implements RouterLayout {
    public MainLayout() {
        createHeader();
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

        Button searchButton = new Button(new Icon(VaadinIcon.SEARCH));
        searchButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        searchLayout.add(searchField, searchButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchButton.addClickListener(event -> performSearch(searchField.getValue())); // Button click
        searchField.addKeyDownListener(Key.ENTER, event -> performSearch(searchField.getValue())); // Enter key 
        searchLayout.setFlexGrow(0.4, searchField);

        // LOGIN/REGISTER BUTTON
        // TODO: IMPLEMENT THAT IF THE USER IS LOGGED IN THE BUTTON WILL TURN TO LOGOUT OR SOMETHING 
        Button loginRegisterButton = new Button("Login/Register");
        loginRegisterButton.addClickListener(event -> loginRegisterClick());

        //NOTIFS
        Icon notificationsIcon = VaadinIcon.BELL.create();
        notificationsIcon.setSize("30px");
        Button notificationsButton = new Button(notificationsIcon);
        notificationsButton.addClickListener(event -> showNotifications());

        // CART
        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.setSize("30px");// TO DO: ADD ANCHOR TO NAVIGATE TO CART
        Button cartButton = new Button(cartIcon);
        cartButton.addClickListener(event -> showCart());

        
        // HEADER LAYOUT
        HorizontalLayout header = new HorizontalLayout(logoAnchor, spacer, searchLayout, notificationsButton, loginRegisterButton, cartButton);
        header.setWidth("100%");
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.getStyle().set("background", "#ffffff").set("padding", "10px");
        header.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, searchLayout);
        addToNavbar(header);
    }








    private void openRegisterDialog() {
        Dialog registerDialog = new Dialog();
        registerDialog.setHeaderTitle("Register");

        FormLayout registerFormLayout = new FormLayout();
        EmailField registerEmailField = new EmailField("Email");
        PasswordField registerPasswordField = new PasswordField("Password");
        PasswordField registerConfirmPasswordField = new PasswordField("Confirm Password");
        registerFormLayout.add(registerEmailField, registerPasswordField, registerConfirmPasswordField);

        Button registerButton = new Button("Register", e -> {
            // TODO: Implement Register logic (validate input PWFIELD = CPWFIELD, create user)
            register();
        });
        registerEmailField.addKeyDownListener(Key.ENTER, event -> register());
        registerPasswordField.addKeyDownListener(Key.ENTER, event -> register());
        registerConfirmPasswordField.addKeyDownListener(Key.ENTER, event -> register());

        registerDialog.add(registerFormLayout);
        registerDialog.getFooter().add(registerButton);
        registerDialog.open();
    }

    private void register(){
        testDialog();
    }

    private void testDialog(){
        Dialog testDialog = new Dialog();
        testDialog.setHeaderTitle("NOT IMPLEMENTED YET");
        testDialog.open();
    }

    private void performSearch(String searchText){
        //TO DO - RN JUST FOR TEST
        if (searchText != null && !searchText.isEmpty()) {
            // Perform the search operation with searchTerm
            Notification.show("Searching for: " + searchText);  // Replace with your actual search logic
        } else {
            Notification.show("Please enter a search term.");
        }
       
    }

    private void showNotifications(){
        testDialog();
    }


    private void showCart(){
        testDialog();
    }

    private void loginRegisterClick(){
        Dialog loginDialog = new Dialog();
        loginDialog.setHeaderTitle("Login");
        FormLayout loginFormLayout = new FormLayout();
        EmailField loginEmailField = new EmailField("Email");
        PasswordField loginPasswordField = new PasswordField("Password");
        loginFormLayout.add(loginEmailField, loginPasswordField);
        loginEmailField.addKeyDownListener(Key.ENTER, e -> login());
        loginPasswordField.addKeyDownListener(Key.ENTER, e -> login());

        Button loginButton = new Button("Login");
        loginButton.addClickListener(e -> login());

        Button registerButton = new Button("Register", e -> {
            loginDialog.close(); // Close the Login Dialog
            openRegisterDialog(); // Open the Register Dialog
        });

        loginDialog.add(loginFormLayout);
        HorizontalLayout loginButtonsLayout = new HorizontalLayout(loginButton, registerButton);
        loginDialog.add(loginButtonsLayout);
        loginDialog.open(); // Open the Login Dialog
    }

    private void login(){
        testDialog();
    }


}
