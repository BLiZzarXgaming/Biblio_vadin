package com.example.application.views;

import com.example.application.config.SecurityService;
import com.example.application.identification_user.MyUserPrincipal;
import com.example.application.service.AvailabilityService;
import com.example.application.views.myview.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.lineawesome.LineAwesomeIcon;

import javax.swing.*;
import java.util.Collection;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private SecurityService securityService;

    private H1 viewTitle;

    public MainLayout(@Autowired SecurityService securityService) {
        this.securityService = securityService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(false, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Menu");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Accueil", HomeView.class));
        nav.addItem(new SideNavItem("Catalogue", CatalogueView.class));
        nav.addItem(new SideNavItem("Testa1", testa1.class));
        nav.addItem(new SideNavItem("Testb1", testb1.class));
        nav.addItem(new SideNavItem("Testm1", testm1.class));


        return nav;
    }

    private Footer createFooter() {
        // Récupérer les rôles de l'utilisateur
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Footer layout = new Footer();
        VerticalLayout footer = new VerticalLayout();


        if (securityService.getAuthenticatedUser() != null) {

            Div div = new Div();
            div.setWidth("100%");
            div.getStyle().set("text-align", "center");

            Icon icon = VaadinIcon.USER.create();
            div.add(icon);
            Div divNameUSer = new Div();
            divNameUSer.setText(((MyUserPrincipal) securityService.getAuthenticatedUser()).getRealName());
            divNameUSer.setWidth("100%");
            divNameUSer.getStyle().set("text-align", "center");

            Button logout = new Button("Se déconnecter", click ->
                    securityService.logout());
            logout.setWidthFull();

            HtmlObject hr = new HtmlObject("hr");
            hr.getStyle().set("height", "1px");
            hr.getStyle().set("width", "100%");
            hr.getStyle().set("background-color", "black");

            footer.add(hr,div, divNameUSer, logout);
        } else {
            Button login = new Button("Se connecter", click ->
                    getUI().ifPresent(ui ->
                            ui.navigate("login")));

            login.setWidthFull();
            footer.add(login);
        }

        layout.add(footer);
        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
