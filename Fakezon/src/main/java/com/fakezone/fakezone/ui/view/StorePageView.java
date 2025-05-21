package com.fakezone.fakezone.ui.view;

import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.DTO.StoreDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Route(value = "/store/:storeId", layout = MainLayout.class)
public class StorePageView extends VerticalLayout implements AfterNavigationObserver{
    private int storeId;
    private StoreDTO storeDto;
    private final RestTemplate restTemplate = new RestTemplate();

    public StorePageView(){

    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        RouteParameters params = event.getRouteParameters();
        this.storeId = Integer.parseInt(params.get("storeId").orElse("0"));
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false); // true = create if not exist
        String token = (String) session.getAttribute("token");
        add(new H1("Stub Store Page for store: " + storeId));
    }
}


