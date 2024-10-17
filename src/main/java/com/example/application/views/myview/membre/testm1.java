package com.example.application.views.myview.membre;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Testm1")
@Route(value = "/testm1", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class testm1 extends Composite<VerticalLayout> {
    public testm1() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
