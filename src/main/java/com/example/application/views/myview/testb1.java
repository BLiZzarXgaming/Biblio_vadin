package com.example.application.views.myview;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Testb1")
@Route(value = "/testb1", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class testb1 extends Composite<VerticalLayout> {
    public testb1() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
