package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

@Route(value = "", layout = MainLayout.class)
public class HomeView extends Main {
    private final RestTemplate restTemplate = new RestTemplate();
    public HomeView() {
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false); // true = create if not exist
        String token = (String) session.getAttribute("token");
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        int bestProdsLim = 4;
        createBestsellersSection(bestProdsLim, token);
    }

    private void createBestsellersSection(int lim, String token) {
        // title
        H1 title = new H1("Top Rated Products");
        title.getStyle().set("margin-bottom", "0.5em");
        String url = "http://localhost:8080/api/product/topRated/" + lim;
        //api call
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response<List<StoreProductDTO>>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<Response<List<StoreProductDTO>>>() {}
        );
        HorizontalLayout cardsRow = new HorizontalLayout();
        cardsRow.setWidthFull();
        cardsRow.setPadding(true);
        cardsRow.setSpacing(true);
        cardsRow.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);

        Response<List<StoreProductDTO>> response = apiResponse.getBody();
        if(response.isSuccess()){
            List<StoreProductDTO> products = response.getData();
            for (StoreProductDTO p : products) {
                VerticalLayout card = new VerticalLayout();
                card.setPadding(false);
                card.setSpacing(false);
                card.getStyle().set("cursor", "pointer");
                card.add(new H4(p.getName()));
                card.add(new Span(p.getCategory().toString()));
                card.add(new Span("Rating: " + p.getAverageRating()));
                card.add(new Span("$" + Double.toString(p.getBasePrice())));

                card.addClickListener(evt -> 
                    UI.getCurrent().navigate(
                        StoreProductView.class,
                        new RouteParameters(new RouteParam("storeId", p.getStoreId()), 
                                            new RouteParam("productId", p.getProductId())
                                                )
                    )
                );

                // ─── Quantity selector ─────────────────────────────
                IntegerField qtyField = new IntegerField("Quantity:");
                qtyField.setStepButtonsVisible(true);
                qtyField.setMin(1);
                qtyField.setMax(p.getQuantity());        // optional: cap at available stock
                qtyField.setValue(1);
                qtyField.setWidth("100px");

                VerticalLayout fullCard = new VerticalLayout();
                fullCard.setWidth("200px");
                fullCard.setHeight("230px");
                fullCard.getStyle()
                    .set("border", "1px solid #999")
                    .set("border-radius", "4px")
                    .set("padding", "0.5em")
                    .set("align-items", "left");
                fullCard.add(card);

                Icon cartIcon = VaadinIcon.CART.create();
                cartIcon.setSize("30px");
                Button cartButton = new Button(cartIcon);
                cartButton.addClickListener(event -> addToCart(p.getStoreId() ,p.getProductId(), qtyField.getValue()));
                HorizontalLayout controls = new HorizontalLayout(qtyField, cartButton);
                controls.setAlignItems(Alignment.END);   // align bottoms
                controls.setSpacing(true);
                controls.setPadding(false);
                fullCard.add(controls);
                
                cardsRow.add(fullCard);
            }
        }
        else {
            // fallback: show an error message
            cardsRow.add(new Span("Unable to load bestsellers: " + response.getMessage()));
        }

        // wrap and add
        VerticalLayout section = new VerticalLayout(title, cardsRow);
        section.setWidth("100%");
        section.setAlignItems(VerticalLayout.Alignment.CENTER);
        section.getStyle().set("padding", "1em");
        add(section);
    }
    
    public static void showMainView() {
        UI.getCurrent().navigate(HomeView.class);
    }

    private void addToCart(int storeId, int prodId, int quantity){
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = (String) session.getAttribute("token");
        //ASSUMING ONLY REGISTERED RIGHT NOW
        UserDTO userDto = (UserDTO) session.getAttribute("userDTO");
        if(userDto == null){
            // TO DO : GUEST
        }
        else{
            int userId = userDto.getUserId();
            String url = "http://localhost:8080/api/user/addToBasket/" + userId +"/"+storeId+"/"+prodId+"/"+quantity;
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
    }
}
