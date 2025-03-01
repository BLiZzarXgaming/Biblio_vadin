package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Historique des Emprunts")
@Route(value = "membre/historique-emprunts", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreHistoriqueEmpruntView extends VerticalLayout {

    private final LoanServiceV2 loanService;
    private final UserServiceV2 userService;

    private Grid<LoanDto> loansGrid;
    private TextField searchField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> statusFilter;

    private Long currentUserId;
    private List<LoanDto> allUserLoans;
    private int pageSize = 10;

    public MembreHistoriqueEmpruntView(LoanServiceV2 loanService, UserServiceV2 userService) {
        this.loanService = loanService;
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Get current user ID from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            Optional<UserDto> currentUser = userService.findByUsername(username);
            if (currentUser.isPresent()) {
                currentUserId = currentUser.get().getId();

                // Create and configure UI components
                createHeader();
                createSearchBar();
                createLoansGrid();

                // Load initial data
                loadUserLoans();
            } else {
                showNotification("Utilisateur non trouvé", "error");
            }
        } else {
            showNotification("Vous devez être connecté pour accéder à cette page", "error");
        }
    }

    private void createHeader() {
        H2 title = new H2("Historique de mes emprunts");
        add(title);
    }

    private void createSearchBar() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();
        searchLayout.setAlignItems(Alignment.BASELINE);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre, auteur, etc.");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateGridItems());

        startDatePicker = new DatePicker("Du");
        startDatePicker.setLocale(new java.util.Locale("fr", "FR"));
        startDatePicker.addValueChangeListener(e -> updateGridItems());

        endDatePicker = new DatePicker("Au");
        endDatePicker.setLocale(new java.util.Locale("fr", "FR"));
        endDatePicker.addValueChangeListener(e -> updateGridItems());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems("Tous", "Emprunté", "Retourné", "En retard", "Annulé");
        statusFilter.setValue("Tous");
        statusFilter.addValueChangeListener(e -> updateGridItems());

        Button resetButton = new Button("Réinitialiser", e -> resetFilters());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        searchLayout.add(searchField, startDatePicker, endDatePicker, statusFilter, resetButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private void createLoansGrid() {
        loansGrid = new Grid<>();
        loansGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Add columns for loan information
        loansGrid.addColumn(loan -> loan.getCopy().getItem().getTitle())
                .setHeader("Titre")
                .setSortable(true)
                .setFlexGrow(1);

        loansGrid.addColumn(loan -> {
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
        }).setHeader("Type").setWidth("120px");

        loansGrid.addColumn(loan -> loan.getLoanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt")
                .setSortable(true)
                .setWidth("150px");

        loansGrid.addColumn(loan -> loan.getReturnDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de retour prévue")
                .setSortable(true)
                .setWidth("180px");

        loansGrid.addComponentColumn(loan -> {
            Span status = new Span(getStatusText(loan.getStatus()));
            status.getElement().getThemeList().add("badge");

            switch (loan.getStatus()) {
                case "borrowed":
                    if (loan.getReturnDueDate().isBefore(LocalDate.now())) {
                        status.setText("En retard");
                        status.getElement().getThemeList().add("error");
                    } else {
                        status.getElement().getThemeList().add("success");
                    }
                    break;
                case "returned":
                    status.getElement().getThemeList().add("contrast");
                    break;
                case "canceled":
                    status.getElement().getThemeList().add("error");
                    break;
                default:
                    status.getElement().getThemeList().add("contrast");
            }

            return status;
        }).setHeader("Statut").setWidth("150px");

        // Configure grid height and pagination
        loansGrid.setHeight("70vh");
        loansGrid.setPageSize(pageSize);

        add(loansGrid);
        setFlexGrow(1, loansGrid);
    }

    private void loadUserLoans() {
        if (currentUserId != null) {
            allUserLoans = loanService.findByMember(currentUserId);
            updateGridItems();
        }
    }

    private void updateGridItems() {
        // Apply filters and set items to grid with pagination
        List<LoanDto> filteredLoans = filterLoans();

        // Create a data provider with server-side pagination
        CallbackDataProvider<LoanDto, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    if (filteredLoans.isEmpty() || offset >= filteredLoans.size()) {
                        return java.util.stream.Stream.empty();
                    }

                    int end = Math.min(offset + limit, filteredLoans.size());
                    return filteredLoans.subList(offset, end).stream();
                },
                query -> filteredLoans.size());

        loansGrid.setDataProvider(dataProvider);

        // Afficher un message si aucun résultat
        if (filteredLoans.isEmpty()) {
            showNotification("Aucun emprunt trouvé avec ces critères", "info");
        }
    }

    private List<LoanDto> filterLoans() {
        if (allUserLoans == null) {
            return Collections.emptyList();
        }

        String searchTerm = searchField.getValue().toLowerCase();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String status = statusFilter.getValue();

        // Create a copy of the list to avoid modification issues
        return allUserLoans.stream()
                .filter(loan -> {
                    // Search in title
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            loan.getCopy().getItem().getTitle().toLowerCase().contains(searchTerm);

                    // Filter by date range
                    boolean matchesDateRange = true;
                    if (startDate != null && loan.getLoanDate().isBefore(startDate)) {
                        matchesDateRange = false;
                    }
                    if (endDate != null && loan.getLoanDate().isAfter(endDate)) {
                        matchesDateRange = false;
                    }

                    // Filter by status
                    boolean matchesStatus = "Tous".equals(status) || matchesStatusFilter(loan, status);

                    return matchesSearch && matchesDateRange && matchesStatus;
                })
                .collect(Collectors.toList());
    }

    private boolean matchesStatusFilter(LoanDto loan, String filter) {
        switch (filter) {
            case "Emprunté":
                return "borrowed".equals(loan.getStatus());
            case "Retourné":
                return "returned".equals(loan.getStatus());
            case "En retard":
                return "borrowed".equals(loan.getStatus()) &&
                        loan.getReturnDueDate().isBefore(LocalDate.now());
            case "Annulé":
                return "canceled".equals(loan.getStatus());
            default:
                return true;
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "borrowed":
                return "Emprunté";
            case "returned":
                return "Retourné";
            case "canceled":
                return "Annulé";
            default:
                return status;
        }
    }

    private void resetFilters() {
        searchField.clear();
        startDatePicker.clear();
        endDatePicker.clear();
        statusFilter.setValue("Tous");
        updateGridItems();
    }

    private void showNotification(String message, String type) {
        Notification notification = new Notification(message, 3000, Notification.Position.MIDDLE);

        switch (type) {
            case "success":
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            case "error":
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
            case "info":
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                break;
            default:
                // No variant
        }

        notification.open();
    }
}