package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import com.vaadin.flow.component.html.Main;

@Route(value = "", layout = MainLayout.class)
public class HomeView extends Main {

    public HomeView() {
         HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        HttpSession session = request.getSession(false); // true = create if not exist

        String token = (String) session.getAttribute("token");

        add(new Button("Click Me", e -> e.getSource().setText(token)));
    }
    public static void showMainView() {
        UI.getCurrent().navigate(HomeView.class);
    }
}
