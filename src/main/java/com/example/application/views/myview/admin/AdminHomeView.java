package com.example.application.views.myview.admin;


import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accueil")
@Route(value = "admin/home", layout = MainLayout.class)
//@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ROLE_ADMINISTRATEUR")
public class AdminHomeView extends Composite<VerticalLayout> {

    public AdminHomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        H2 title = new H2("Bibliothèque admin Anne-Marie Doyon");

        getContent().add(title);
    }
}
