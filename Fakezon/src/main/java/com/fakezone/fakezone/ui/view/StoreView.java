package com.fakezone.fakezone.ui.view;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.Response;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Route(value = "store", layout = MainLayout.class)
public class StoreView extends VerticalLayout implements BeforeEnterObserver{

    private final String backendUrl = "http://localhost:8080";
    private final RestTemplate restTemplate;

    public StoreView() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        HttpServletRequest httpRequest = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = httpRequest.getSession(false);
        String token = session != null ? (String) session.getAttribute("token") : null;

        if (token == null) {
            event.rerouteTo("login");
            return;
        }

        if (isGuestToken(session)) {
            event.rerouteTo("login");
            return;
        }

        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = queryParameters.getParameters();
        List<String> storeIdParam = parameters.getOrDefault("storeId", List.of());

        if (storeIdParam.isEmpty()) {
            add(new Span("No store ID provided."));
            return;
        }

        int storeId;
        try {
            storeId = Integer.parseInt(storeIdParam.get(0));
        } catch (NumberFormatException e) {
            add(new Span("Invalid store ID."));
            return;
        }

        StoreDTO store = getStoreDTO(storeId, token);
        if (store == null) {
            add(new Span("Failed to load store with ID: " + storeId));
            return;
        }

        add(new H2("Store Details"));
        add(new Span("Name: " + store.getName()));
        add(new Span("ID: " + store.getStoreId()));
        add(new Span("Open: " + (store.isOpen() ? "Yes" : "No")));
        add(new Span("Average Rating: " + store.getRatings().values().stream().mapToDouble(d -> d).average().orElse(0.0)));
    }

    private boolean isGuestToken(HttpSession session) {
        String token = (String) session.getAttribute("token");
        RestTemplate restTemplate = new RestTemplate();
        String url = backendUrl + "/api/user/isGuestToken";
        try {
            ResponseEntity<Response> apiResponse = restTemplate.postForEntity(url, token, Response.class);
            Response<Boolean> response = (Response<Boolean>) apiResponse.getBody();
            return response.isSuccess() && response.getData();
        } catch (Exception e) {
            return true;
        }
    }

    private StoreDTO getStoreDTO(int storeId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = String.format(backendUrl + "/api/store/viewStore/%d", storeId);

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
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}