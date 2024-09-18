package com.example.application.views.myview;


import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Testa1")
@Route(value = "/testa1", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class testa1 extends Composite<VerticalLayout> {
    public testa1() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
