package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.BookDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.ReservationDto;
import com.example.application.service.implementation.BoardGameServiceV2;
import com.example.application.service.implementation.BookServiceV2;
import com.example.application.service.implementation.CopyServiceV2;
import com.example.application.service.implementation.ItemServiceV2;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.MagazineServiceV2;
import com.example.application.service.implementation.ReservationServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Accueil")
@Route(value = "volunteer/home", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleHomeView extends Composite<VerticalLayout> {

    private final ItemServiceV2 itemService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final LoanServiceV2 loanService;
    private final ReservationServiceV2 reservationService;
    private final CopyServiceV2 copyService;

    public BenevoleHomeView(
            ItemServiceV2 itemService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            LoanServiceV2 loanService,
            ReservationServiceV2 reservationService,
            CopyServiceV2 copyService) {

        this.itemService = itemService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.loanService = loanService;
        this.reservationService = reservationService;
        this.copyService = copyService;

        configureLayout();
    }

    private void configureLayout() {
        VerticalLayout content = getContent();
        content.setWidth("100%");
        content.setHeightFull();
        content.setPadding(true);
        content.setSpacing(true);

        H2 title = new H2("Tableau de bord - Bibliothèque Anne-Marie Doyon");
        title.getStyle().set("margin-top", "0");
        content.add(title);

        // Date d'aujourd'hui
        Paragraph dateInfo = new Paragraph("Aujourd'hui: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        dateInfo.getStyle().set("font-style", "italic");
        dateInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        content.add(dateInfo);

        // Layout pour les widgets statistiques
        HorizontalLayout statsLayout = createStatsLayout();
        content.add(statsLayout);

        // Widget pour les nouveautés
        Component recentItems = createRecentItemsWidget();
        content.add(recentItems);

        // Widget pour les prêts à échéance aujourd'hui
        Component dueTodayWidget = createDueTodayWidget();
        content.add(dueTodayWidget);

        // Ajouter les boutons d'actions rapides
        HorizontalLayout quickActions = createQuickActionsLayout();
        content.add(quickActions);
    }

    private HorizontalLayout createStatsLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.getStyle().set("flex-wrap", "wrap");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Aujourd'hui
        LocalDate today = LocalDate.now();

        // Statistique: Nombre d'emprunts actifs
        int activeLoansCount = countActiveLoans();
        layout.add(createStatWidget("Emprunts actifs",
                String.valueOf(activeLoansCount),
                VaadinIcon.BOOK,
                "rgb(100, 181, 246)"));

        // Statistique: Nombre de prêts en retard
        int overdueLoansCount = countOverdueLoans();
        layout.add(createStatWidget("Retards",
                String.valueOf(overdueLoansCount),
                VaadinIcon.TIME_BACKWARD,
                "rgb(239, 83, 80)"));

        // Statistique: Nombre de retours aujourd'hui
        int todayReturnsCount = countTodayReturns();
        layout.add(createStatWidget("Retours aujourd'hui",
                String.valueOf(todayReturnsCount),
                VaadinIcon.ARROW_BACKWARD,
                "rgb(129, 199, 132)"));

        // Statistique: Nombre de réservations à traiter
        int pendingReservationsCount = countPendingReservations();
        layout.add(createStatWidget("Réservations en attente",
                String.valueOf(pendingReservationsCount),
                VaadinIcon.CALENDAR_CLOCK,
                "rgb(255, 183, 77)"));

        return layout;
    }

    private Component createStatWidget(String title, String value, VaadinIcon iconType, String color) {
        Div widget = new Div();
        widget.addClassName("stat-widget");
        widget.getStyle()
                .set("background-color", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.1)")
                .set("padding", "16px")
                .set("text-align", "center")
                .set("min-width", "200px")
                .set("margin", "8px")
                .set("flex-grow", "1")
                .set("border-top", "4px solid " + color);

        // Icône
        Icon icon = iconType.create();
        icon.setSize("32px");
        icon.setColor(color);

        // Valeur
        H2 valueH2 = new H2(value);
        valueH2.getStyle()
                .set("margin", "8px 0")
                .set("font-size", "38px")
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        // Titre
        Paragraph titleParagraph = new Paragraph(title);
        titleParagraph.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px");

        widget.add(icon, valueH2, titleParagraph);
        return widget;
    }

    private Component createRecentItemsWidget() {
        Div widget = new Div();
        widget.addClassName("recent-items-widget");
        widget.getStyle()
                .set("background-color", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.1)")
                .set("padding", "16px")
                .set("margin", "16px 0")
                .set("width", "100%");

        H3 title = new H3("Nouveaux documents");
        title.getStyle().set("margin-top", "0");

        // Créer une grille pour afficher les documents récents
        Grid<ItemDto> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("250px");

        grid.addColumn(ItemDto::getTitle).setHeader("Titre").setFlexGrow(2);
        grid.addColumn(item -> {
            switch (item.getType()) {
                case "book":
                    return "Livre";
                case "magazine":
                    return "Revue";
                case "board_game":
                    return "Jeu";
                default:
                    return item.getType();
            }
        }).setHeader("Type").setFlexGrow(1);
        grid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie").setFlexGrow(1);
        grid.addColumn(item -> item.getCreatedAt() != null ? DateTimeFormatter.ofPattern("dd/MM/yyyy").format(
                item.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                : "").setHeader("Date d'ajout").setFlexGrow(1);

        // Récupérer les 10 documents les plus récents
        List<ItemDto> recentItems = getRecentItems(10);
        grid.setItems(recentItems);

        Button viewMoreButton = new Button("Voir tout le catalogue",
                e -> getUI().ifPresent(ui -> ui.navigate("volunteer/catalogue")));
        viewMoreButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        widget.add(title, grid, viewMoreButton);
        return widget;
    }

    private Component createDueTodayWidget() {
        Div widget = new Div();
        widget.addClassName("due-today-widget");
        widget.getStyle()
                .set("background-color", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.1)")
                .set("padding", "16px")
                .set("margin", "16px 0")
                .set("width", "100%");

        H3 title = new H3("Retours prévus aujourd'hui");
        title.getStyle().set("margin-top", "0");

        List<LoanDto> dueTodayLoans = getLoansReturnDueToday();

        // Afficher un message s'il n'y a pas de retours attendus
        if (dueTodayLoans.isEmpty()) {
            Paragraph noReturnsMessage = new Paragraph("Aucun retour n'est prévu pour aujourd'hui.");
            noReturnsMessage.getStyle().set("font-style", "italic");
            widget.add(title, noReturnsMessage);
            return widget;
        }

        // Sinon afficher les retours attendus
        Grid<LoanDto> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("200px");

        grid.addColumn(loan -> loan.getMember().getFirstName() + " " + loan.getMember().getLastName())
                .setHeader("Membre").setFlexGrow(1);
        grid.addColumn(loan -> loan.getCopy().getItem().getTitle())
                .setHeader("Document").setFlexGrow(2);
        grid.addColumn(loan -> {
            String type = loan.getCopy().getItem().getType();
            switch (type) {
                case "book":
                    return "Livre";
                case "magazine":
                    return "Revue";
                case "board_game":
                    return "Jeu";
                default:
                    return type;
            }
        }).setHeader("Type").setFlexGrow(1);
        grid.addColumn(loan -> loan.getLoanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt").setFlexGrow(1);

        grid.setItems(dueTodayLoans);

        Button viewAllButton = new Button("Voir tous les retours",
                e -> getUI().ifPresent(ui -> ui.navigate("volunteer/return")));
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        widget.add(title, grid, viewAllButton);
        return widget;
    }

    private HorizontalLayout createQuickActionsLayout() {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidthFull();
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actionsLayout.setSpacing(true);
        actionsLayout.getStyle().set("margin-top", "16px");

        Button newLoanButton = createActionButton("Nouvel emprunt", VaadinIcon.BOOK, "volunteer/loan");
        Button returnButton = createActionButton("Retour", VaadinIcon.ARROW_BACKWARD, "volunteer/return");
        Button catalogueButton = createActionButton("Catalogue", VaadinIcon.SEARCH, "volunteer/catalogue-management");
        Button addItemButton = createActionButton("Ajouter", VaadinIcon.PLUS, "volunteer/add");
        Button membersButton = createActionButton("Membres", VaadinIcon.USERS, "volunteer/members");
        Button reservationsButton = createActionButton("Réservations", VaadinIcon.CALENDAR_CLOCK,
                "volunteer/reservations");

        actionsLayout.add(newLoanButton, returnButton, catalogueButton, addItemButton, membersButton,
                reservationsButton);
        return actionsLayout;
    }

    private Button createActionButton(String text, VaadinIcon icon, String route) {
        Button button = new Button(text, icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.getStyle()
                .set("border-radius", "8px")
                .set("margin", "8px")
                .set("width", "160px")
                .set("height", "100px");

        button.setIconAfterText(false);
        button.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));
        return button;
    }

    // Méthodes pour récupérer les données

    private int countActiveLoans() {
        return loanService.findByStatus("borrowed").size();
    }

    private int countOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loanService.findByStatus("borrowed").stream()
                .filter(loan -> loan.getReturnDueDate().isBefore(today))
                .collect(Collectors.toList())
                .size();
    }

    private int countTodayReturns() {
        LocalDate today = LocalDate.now();
        return loanService.findAll().stream()
                .filter(loan -> "returned".equals(loan.getStatus()) &&
                        loan.getReturnDueDate().equals(today))
                .collect(Collectors.toList())
                .size();
    }

    private int countPendingReservations() {
        return reservationService.findByStatus("reserved").size();
    }

    private List<ItemDto> getRecentItems(int limit) {
        // Obtenir tous les éléments, puis les trier par date de création
        return itemService.findAll().stream()
                .sorted(Comparator.comparing(ItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<LoanDto> getLoansReturnDueToday() {
        LocalDate today = LocalDate.now();
        return loanService.findByStatus("borrowed").stream()
                .filter(loan -> loan.getReturnDueDate().equals(today))
                .collect(Collectors.toList());
    }
}