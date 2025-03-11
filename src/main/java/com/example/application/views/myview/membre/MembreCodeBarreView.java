package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.UserDto;
import com.example.application.identification_user.MyUserPrincipal;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.barcodes.Barcode;

import java.util.Optional;

@PageTitle("Mon Code Membre")
@Route(value = "membre/codebarre", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreCodeBarreView extends VerticalLayout {

    private final UserServiceV2 userService;
    private UserDto currentUser;

    @Autowired
    public MembreCodeBarreView( UserServiceV2 userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        initCurrentUser();
        add(createHeader(), createBarcodeDisplay());
    }

    private void initCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        currentUser = userService.findByUsername(username).orElse(null);

    }

    private Component createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Mon Code Membre");
        header.add(title);

        if (currentUser != null) {
            H3 username = new H3(currentUser.getFirstName() + " " + currentUser.getLastName());
            header.add(username);

            Paragraph info = new Paragraph(
                    "Voici votre code-barres personnel. Présentez-le lors de vos visites à la bibliothèque.");
            header.add(info);
        }

        return header;
    }

    private Component createBarcodeDisplay() {
        Div container = new Div();
        container.getStyle().set("display", "flex");
        container.getStyle().set("flex-direction", "column");
        container.getStyle().set("align-items", "center");
        container.getStyle().set("justify-content", "center");
        container.getStyle().set("margin-top", "2em");

        if (currentUser != null) {
            // Création du code-barres avec le nom d'utilisateur
            String username = currentUser.getUsername();

            // Utilisation du type de code-barres "code128" - un des types les plus utilisés
            // dans les bibliothèques
            Barcode barcode = new Barcode(username, Barcode.Type.code128, "300px", "150px");

            // Ajouter le nom d'utilisateur sous le code-barres
            Paragraph usernameText = new Paragraph(username);
            usernameText.getStyle().set("font-weight", "bold");
            usernameText.getStyle().set("margin-top", "1em");

            container.add(barcode, usernameText);
        } else {
            Paragraph error = new Paragraph("Impossible d'afficher le code-barres. Veuillez vous reconnecter.");
            container.add(error);
        }

        return container;
    }

    private void showErrorMessage(String message) {
        Paragraph error = new Paragraph(message);
        error.getStyle().set("color", "red");
        add(error);
    }
}