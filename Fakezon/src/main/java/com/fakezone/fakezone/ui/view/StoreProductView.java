package com.fakezone.fakezone.ui.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductRatingDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
@Route(value = "store/:storeId/product/:productId", layout = MainLayout.class)
public class StoreProductView extends VerticalLayout implements AfterNavigationObserver{
    private int storeId;
    private int productId;
    private StoreProductDTO prodDto;
    private H2 title = new H2();
    private Paragraph description = new Paragraph();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;

    public StoreProductView(@Value("${api.url}") String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false); // true = create if not exist
        String token = (String) session.getAttribute("token");
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        RouteParameters params = event.getRouteParameters();
        storeId = Integer.parseInt(params.get("storeId").orElse("0"));
        productId = Integer.parseInt(params.get("productId").orElse("0"));
        title.setText("STUB PAGE FOR PRODUCT " + productId);
        description.setText("STORE ID " + storeId);
        String url = apiUrl + "product/getProductFromStore/" + storeId+"/"+productId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response<StoreProductDTO>> apiResponse = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<Response<StoreProductDTO>>() {}
        );
        Response<StoreProductDTO> response = apiResponse.getBody();
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        if(response.isSuccess()){
            prodDto = response.getData();
            add(new H2(prodDto.getName()));
            add(new H4(prodDto.getCategory().toString()));
            H5 storePage = new H5("Go To Store Page");
            storePage.addClickListener(e ->  UI.getCurrent().navigate("store/"+ prodDto.getStoreId()));
            storePage.getStyle().set("cursor", "pointer");
            add(storePage);
            add(new Span("Price: " + "$" +prodDto.getBasePrice()));
            IntegerField qtyField = new IntegerField("Quantity:");
            qtyField.setStepButtonsVisible(true);
            qtyField.setMin(1);
            qtyField.setMax(prodDto.getQuantity());        // optional: cap at available stock
            qtyField.setValue(1);
            qtyField.setWidth("100px");
            Icon cartIcon = VaadinIcon.CART.create();
            cartIcon.setSize("30px");
            Button cartButton = new Button(cartIcon);
            cartButton.addClickListener(e -> addToCart(prodDto.getStoreId() ,prodDto.getProductId(), qtyField.getValue()));
            if(prodDto.getQuantity() == 0){
                qtyField.setEnabled(false);
                cartButton.setEnabled(false);
            }
            HorizontalLayout controls = new HorizontalLayout(qtyField, cartButton);
            controls.setAlignItems(Alignment.END);   
            controls.setSpacing(true);
            controls.setPadding(false);
            add(controls);
            url = apiUrl + "product/getStoreProductRatings/" + storeId + "/" + productId;
            headers.set("Authorization", token);
            entity = new HttpEntity<>(headers);
            ResponseEntity<Response<List<ProductRatingDTO>>> apiRES = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Response<List<ProductRatingDTO>>>() {}
            );
            Response<List<ProductRatingDTO>> ratingsRES = apiRES.getBody();
            if(ratingsRES.isSuccess()){
                List<ProductRatingDTO> ratings = ratingsRES.getData();
                if(ratings == null || ratings.isEmpty()){
                    add(new Span("NO REVIEWS"));
                }
                else{
                    add(new H3("Average Rating: " + prodDto.getAverageRating()));
                    for (ProductRatingDTO r : ratings){
                        HorizontalLayout review = new HorizontalLayout();
                        review.setWidthFull();
                        review.add(new H4(maskEmail(r.getUserEmail())));
                        review.add(new Span(r.getComment()));
                        review.add(new Span(Double.toString(r.getRating())));
                        add(review);
                    }
                }
            }
            else{
                add(ratingsRES.getMessage());
            }

        }
        else{
            add(response.getMessage());
        }

    }

    private String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 1) {
            // no '@' or nothing before it
            return email;
        }

        String local = email.substring(0, at);
        String domain = email.substring(at); // includes '@'

        // if local-part is very short, mask it all
        if (local.length() <= 3) {
            return "***" + domain;
        }

        // otherwise show first 3 chars, then '***', then '@domain'
        String prefix = local.substring(0, 3);
        return prefix + "***" + domain;
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
    }
}
