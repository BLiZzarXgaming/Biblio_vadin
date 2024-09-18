package com.example.application.components;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Tag("form")
public class LoginFormCusto extends HtmlContainer {

        private boolean hasError = false;

        public LoginFormCusto() {
            VaadinIcon.KEY.create();
            addClassNames(LumoUtility.Display.FLEX,
                    LumoUtility.FlexDirection.COLUMN, LumoUtility.AlignItems.START);

            TextField user = new TextField("Nom d'utilisateur");
            user.setRequired(true);

            PasswordField pass = new PasswordField("Mot de passe");
            pass.setRequired(true);
            pass.isInvalid();

            Button login = new Button("Connexion");
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            login.getElement().setAttribute("type", "submit"); // on-click


            getElement().setAttribute("method", "POST");
            getElement().setAttribute("action", "login");

            Text error = new Text("Nom d'utilisateur ou mot de passe incorrect");

            if (hasError) {
                add(error);
            }
            add(user, pass, login);
        }

        public void setError(boolean hasError) {
            this.hasError = hasError;
        }
}
