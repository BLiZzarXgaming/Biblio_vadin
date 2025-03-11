package com.example.application.views.myview;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.awt.*;

@PageTitle("Connexion")
@Route(value = "login", layout = MainLayout.class)
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private LoginForm login = new LoginForm();


    public LoginView() {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();

        i18n.getForm().setTitle("Connexion");
        i18n.getForm().setUsername("Nom d'utilisateur");
        i18n.getForm().setPassword("Mot de passe");
        i18n.getForm().setSubmit("Connexion");

        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Nom d'utilisateur ou mot de passe incorrect");
        i18nErrorMessage.setMessage("Veuillez vérifier votre nom d'utilisateur et votre mot de passe et réessayer.");
        i18n.setErrorMessage(i18nErrorMessage);

        login.setI18n(i18n);


        add(new H1("Connexion"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {

            login.setError(true);
        }


    }
}
