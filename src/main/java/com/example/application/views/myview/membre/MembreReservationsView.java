package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.User;
import com.example.application.service.implementation.ReservationServiceV2;
import com.example.application.service.implementation.UserRelationshipService;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Mes Réservations")
@Route(value = "member/reservations", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreReservationsView extends VerticalLayout {

    private final ReservationServiceV2 reservationService;
    private final UserServiceV2 userService;
    private final UserRelationshipService userRelationshipService;

    private UserDto currentUser;
    private List<UserDto> children = new ArrayList<>();

    // UI Components
    private Tab myReservationsTab;
    private Tab childrenReservationsTab;
    private Tabs reservationTabs;
    private VerticalLayout contentLayout;
    private Grid<ReservationDto> reservationsGrid;
    private VerticalLayout childrenTabsLayout;
    private Tabs childrenTabs;
    private List<Tab> childrenTabsList = new ArrayList<>();

    public MembreReservationsView(ReservationServiceV2 reservationService,
            UserServiceV2 userService,
            UserRelationshipService userRelationshipService) {
        this.reservationService = reservationService;
        this.userService = userService;
        this.userRelationshipService = userRelationshipService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Initialize the current user
        initializeCurrentUser();

        // Create the UI
        H2 title = new H2("Gestion de mes réservations");
        add(title);

        // Create tabs for navigation
        createTabLayout();

        // Create content area
        contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setPadding(true);

        // Create grid for reservations
        createReservationsGrid();

        // Add the grid to content layout
        contentLayout.add(reservationsGrid);
        add(contentLayout);

        // Load current user's reservations by default
        loadUserReservations(currentUser.getId());
    }

    private void initializeCurrentUser() {
        // Get current logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<UserDto> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            showNotification("Impossible de récupérer les informations de l'utilisateur", "error");
            return;
        }

        currentUser = userOpt.get();

        // Get children if any
        User user = new User();
        user.setId(currentUser.getId());
        List<User> childrenEntities = userRelationshipService.findChildrenByParentId(currentUser.getId());

        for (User child : childrenEntities) {
            Optional<UserDto> childDto = userService.findById(child.getId());
            childDto.ifPresent(children::add);
        }
    }

    private void createTabLayout() {
        HorizontalLayout tabLayout = new HorizontalLayout();
        tabLayout.setWidthFull();

        // Create main tabs
        myReservationsTab = new Tab("Mes réservations");
        childrenReservationsTab = new Tab("Réservations des enfants");

        // Make children tab visible only if the user has children
        childrenReservationsTab.setVisible(!children.isEmpty());

        reservationTabs = new Tabs(myReservationsTab, childrenReservationsTab);
        reservationTabs.setWidthFull();

        // Add listener for main tabs
        reservationTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(myReservationsTab)) {
                showMyReservations();
            } else if (event.getSelectedTab().equals(childrenReservationsTab)) {
                showChildrenReservationTabs();
            }
        });

        tabLayout.add(reservationTabs);
        add(tabLayout);

        // Create layout for children tabs (initially hidden)
        childrenTabsLayout = new VerticalLayout();
        childrenTabsLayout.setPadding(false);
        childrenTabsLayout.setSpacing(false);
        childrenTabsLayout.setVisible(false);

        // Create children tabs if there are children
        if (!children.isEmpty()) {
            childrenTabs = new Tabs();
            childrenTabs.setWidthFull();

            // Create a tab for each child
            for (UserDto child : children) {
                Tab childTab = new Tab(child.getFirstName() + " " + child.getLastName());
                childrenTabsList.add(childTab);
                childrenTabs.add(childTab);
            }

            // Add listener for children tabs
            childrenTabs.addSelectedChangeListener(event -> {
                int index = childrenTabsList.indexOf(event.getSelectedTab());
                if (index >= 0 && index < children.size()) {
                    loadUserReservations(children.get(index).getId());
                }
            });

            childrenTabsLayout.add(childrenTabs);
        }

        add(childrenTabsLayout);
    }

    private void createReservationsGrid() {
        reservationsGrid = new Grid<>();
        reservationsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        reservationsGrid.setHeight("100%");

        // Add columns
        reservationsGrid.addColumn(reservation -> reservation.getCopy().getItem().getTitle()).setHeader("Titre")
                .setFlexGrow(1);

        reservationsGrid.addColumn(reservation -> {
            String type = reservation.getCopy().getItem().getType();
            return switch (type) {
                case "book" -> "Livre";
                case "magazine" -> "Revue";
                case "board_game" -> "Jeu";
                default -> type;
            };
        }).setHeader("Type").setWidth("120px").setFlexGrow(0);

        reservationsGrid
                .addColumn(reservation -> reservation.getReservationDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de réservation").setWidth("150px").setFlexGrow(0);

        // Status column with colored badges
        reservationsGrid.addColumn(new ComponentRenderer<>(reservation -> {
            Span statusBadge = new Span();
            statusBadge.getElement().getThemeList().add("badge");

            switch (reservation.getStatus()) {
                case "reserved":
                    statusBadge.setText("En attente");
                    statusBadge.getElement().getThemeList().add("contrast");
                    break;
                case "ready":
                    statusBadge.setText("Prêt à récupérer");
                    statusBadge.getElement().getThemeList().add("success");
                    break;
                case "cancelled":
                    statusBadge.setText("Annulée");
                    statusBadge.getElement().getThemeList().add("error");
                    break;
                default:
                    statusBadge.setText(reservation.getStatus());
            }

            return statusBadge;
        })).setHeader("Statut").setWidth("150px").setFlexGrow(0);

        // Actions column
        reservationsGrid.addComponentColumn(reservation -> {
            // Only show cancel button for active reservations
            if (!"cancelled".equals(reservation.getStatus())) {
                Button cancelButton = new Button("Annuler");
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.addClickListener(e -> confirmCancelReservation(reservation));
                return cancelButton;
            }
            return new Span(); // Empty span for cancelled reservations
        }).setHeader("Actions").setWidth("120px").setFlexGrow(0);
    }

    private void showMyReservations() {
        childrenTabsLayout.setVisible(false);
        contentLayout.removeAll();
        contentLayout.add(new H3("Mes réservations"), reservationsGrid);
        loadUserReservations(currentUser.getId());
    }

    private void showChildrenReservationTabs() {
        if (children.isEmpty()) {
            showNotification("Vous n'avez pas d'enfants associés à votre compte", "info");
            reservationTabs.setSelectedTab(myReservationsTab);
            return;
        }

        childrenTabsLayout.setVisible(true);

        // Select the first child by default
        if (!childrenTabsList.isEmpty() && childrenTabs.getSelectedTab() == null) {
            childrenTabs.setSelectedIndex(0);
        }

        // Update title and load reservations
        int selectedIndex = childrenTabs.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < children.size()) {
            UserDto selectedChild = children.get(selectedIndex);
            contentLayout.removeAll();
            contentLayout.add(new H3("Réservations de " + selectedChild.getFirstName()), reservationsGrid);
            loadUserReservations(selectedChild.getId());
        }
    }

    private void loadUserReservations(Long userId) {
        List<ReservationDto> reservations = reservationService.findByMember(userId);
        reservationsGrid.setItems(reservations);

        if (reservations.isEmpty()) {
            showNotification("Aucune réservation trouvée", "info");
        }
    }

    private void confirmCancelReservation(ReservationDto reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer l'annulation");
        dialog.setText("Êtes-vous sûr de vouloir annuler cette réservation ?");

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> cancelReservation(reservation));

        dialog.open();
    }

    private void cancelReservation(ReservationDto reservation) {
        try {
            // Update reservation status to cancelled
            reservation.setStatus("cancelled");
            reservationService.save(reservation);

            // Update copy status to available
            reservation.getCopy().setStatus("available");

            // Refresh grid data
            if (reservation.getMember().getId().equals(currentUser.getId())) {
                loadUserReservations(currentUser.getId());
            } else {
                // Find which child this reservation belongs to
                for (UserDto child : children) {
                    if (child.getId().equals(reservation.getMember().getId())) {
                        loadUserReservations(child.getId());
                        break;
                    }
                }
            }

            showNotification("Réservation annulée avec succès", "success");
        } catch (Exception e) {
            showNotification("Erreur lors de l'annulation: " + e.getMessage(), "error");
        }
    }

    private void showNotification(String message, String type) {
        Notification notification = new Notification(message);
        notification.setDuration(3000);

        switch (type) {
            case "success":
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            case "error":
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
            case "warning":
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                break;
            case "info":
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                break;
        }

        notification.open();
    }
}