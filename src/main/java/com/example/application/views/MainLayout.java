package com.example.application.views;

import com.example.application.config.SecurityService;
import com.example.application.identification_user.MyUserPrincipal;
import com.example.application.views.myview.*;
import com.example.application.views.myview.admin.AdminCalendarView;
import com.example.application.views.myview.admin.AdminGestionBenevolesView;
import com.example.application.views.myview.admin.AdminHomeView;
import com.example.application.views.myview.admin.AdminLimitsView;
import com.example.application.views.myview.admin.AdminManagementView;
import com.example.application.views.myview.admin.AdminStatisticsView;
import com.example.application.views.myview.benevole.BenevoleAjouterView;
import com.example.application.views.myview.benevole.BenevoleCatalogueView;
import com.example.application.views.myview.benevole.BenevoleCodeBarreView;
import com.example.application.views.myview.benevole.BenevoleEmpruntView;
import com.example.application.views.myview.benevole.BenevoleHomeView;
import com.example.application.views.myview.benevole.BenevoleListeMembresView;
import com.example.application.views.myview.benevole.BenevoleListeReservationView;
import com.example.application.views.myview.benevole.BenevoleRetardsView;
import com.example.application.views.myview.benevole.BenevoleRetourView;
import com.example.application.views.myview.membre.MembreCatalogueView;
import com.example.application.views.myview.membre.MembreEmpruntsView;
import com.example.application.views.myview.membre.MembreHistoriqueEmpruntView;
import com.example.application.views.myview.membre.MembreReservationsView;
import com.example.application.views.myview.membre.MembreCodeBarreView;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (securityService.getAuthenticatedUser() == null) {
            return createNavigationAnonyme(nav);
        } else {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMINISTRATEUR")) {
                    return createNavigationAdmin(nav);
                } else if (authority.getAuthority().equals("ROLE_MEMBRE")) {
                    return createNavigationMembre(nav);
                } else if (authority.getAuthority().equals("ROLE_BÉNÉVOLE")) {
                    return createNavigationBenevole(nav);
                }
            }
        }

        return nav;
    }

    private SideNav createNavigationAnonyme(SideNav nav) {

        nav.addItem(new SideNavItem("Accueil", HomeView.class));
        nav.addItem(new SideNavItem("Catalogue", CatalogueView.class));

        return nav;
    }

    private SideNav createNavigationMembre(SideNav nav) {

        nav.addItem(new SideNavItem("Accueil", HomeView.class));
        nav.addItem(new SideNavItem("Catalogue", MembreCatalogueView.class));
        nav.addItem(new SideNavItem("Mes emprunts", MembreEmpruntsView.class));
        nav.addItem(new SideNavItem("Mes réservations", MembreReservationsView.class));
        nav.addItem(new SideNavItem("Mon historique", MembreHistoriqueEmpruntView.class));
        nav.addItem(new SideNavItem("Mon code membre", MembreCodeBarreView.class));

        return nav;
    }

    private SideNav createNavigationBenevole(SideNav nav) {

        nav.addItem(new SideNavItem("Accueil", BenevoleHomeView.class));
        nav.addItem(new SideNavItem("Catalogue", BenevoleCatalogueView.class));
        nav.addItem(new SideNavItem("Ajout", BenevoleAjouterView.class));
        nav.addItem(new SideNavItem("Emprunts", BenevoleEmpruntView.class));
        nav.addItem(new SideNavItem("Retours", BenevoleRetourView.class));
        nav.addItem(new SideNavItem("Membres", BenevoleListeMembresView.class));
        nav.addItem(new SideNavItem("Réservations", BenevoleListeReservationView.class));
        nav.addItem(new SideNavItem("Retards", BenevoleRetardsView.class));
        nav.addItem(new SideNavItem("Codes-barres", BenevoleCodeBarreView.class));

        return nav;
    }

    private SideNav createNavigationAdmin(SideNav nav) {

        nav.addItem(new SideNavItem("Accueil", AdminHomeView.class));
        nav.addItem(new SideNavItem("Statistiques", AdminStatisticsView.class));
        nav.addItem(new SideNavItem("Horaire", AdminCalendarView.class));
        nav.addItem(new SideNavItem("Liste Bénévoles", AdminGestionBenevolesView.class));
        nav.addItem(new SideNavItem("Limites", AdminLimitsView.class));
        nav.addItem(new SideNavItem("Gestion admins", AdminManagementView.class));
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

            Button logout = new Button("Se déconnecter", click -> securityService.logout());
            logout.setWidthFull();

            HtmlObject hr = new HtmlObject("hr");
            hr.getStyle().set("height", "1px");
            hr.getStyle().set("width", "100%");
            hr.getStyle().set("background-color", "black");

            footer.add(hr, div, divNameUSer, logout);
        } else {
            Button login = new Button("Se connecter", click -> getUI().ifPresent(ui -> ui.navigate("login")));

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
