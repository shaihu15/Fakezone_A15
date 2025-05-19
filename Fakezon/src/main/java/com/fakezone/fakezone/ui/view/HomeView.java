package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
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
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;

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
        H1 title = new H1("Bestsellers");
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
            cardsRow.add(new Span(Integer.toString(products.size())));
            for (StoreProductDTO p : products) {
                VerticalLayout card = new VerticalLayout();
                card.setWidth("150px");
                card.setHeight("200px");
                card.getStyle()
                    .set("border", "1px solid #999")
                    .set("border-radius", "4px")
                    .set("padding", "0.5em")
                    .set("align-items", "center");
                card.add(new Span(p.getName()));
                card.add(new Span("$" + Double.toString(p.getBasePrice())));
                cardsRow.add(card);
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
}
