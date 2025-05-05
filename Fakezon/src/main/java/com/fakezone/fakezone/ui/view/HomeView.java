package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import com.vaadin.flow.component.html.Main;

@Route(value = "", layout = MainLayout.class)
public class HomeView extends Main {

    public HomeView() {
        add(new Button("Click Me", e -> e.getSource().setText("Clicked!")));
    }
    public static void showMainView() {
        UI.getCurrent().navigate(HomeView.class);
    }
}
