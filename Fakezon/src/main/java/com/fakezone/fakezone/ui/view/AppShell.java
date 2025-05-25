package com.fakezone.fakezone.ui.view;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.stereotype.Component;

@Component       // so Spring can pick it up
@Push            // enables Vaadin Push (WebSocket/XHR) for the whole app
public class AppShell implements AppShellConfigurator {
    // no methods needed here
}