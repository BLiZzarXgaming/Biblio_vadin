package com.example.application.views.myview.benevole;


import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accueil")
@Route(value = "volunteer/home", layout = MainLayout.class)
//@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleHomeView extends Composite<VerticalLayout> {


    public BenevoleHomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        H2 title = new H2("Bibliothèque bénévole Anne-Marie Doyon");

        getContent().add(title);
    }
}
