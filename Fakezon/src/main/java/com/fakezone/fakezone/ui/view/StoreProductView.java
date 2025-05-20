package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
@Route("store/:storeId/product/:productId")
public class StoreProductView extends VerticalLayout implements AfterNavigationObserver{
    private String storeId;
    private String productId;
    private H2 title = new H2();
    private Paragraph description = new Paragraph();

    public StoreProductView() {
            setSizeFull();
            setPadding(true);
            setSpacing(true);
            add(title, description);
        }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        RouteParameters params = event.getRouteParameters();
        storeId = params.get("storeId").orElse("0");
        productId = params.get("productId").orElse("0");
        title.setText("STUB PAGE FOR PRODUCT " + productId);
        description.setText("STORE ID " + storeId);
    }
}
