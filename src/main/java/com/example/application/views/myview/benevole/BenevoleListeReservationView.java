package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.service.implementation.ReservationServiceV2;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Réservations")
@Route(value = "volunteer/reservations", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleListeReservationView extends VerticalLayout {

    private final ReservationServiceV2 reservationService;

    // Composants UI
    private TextField searchField;
    private Grid<UserDto> membersGrid;
    private Button refreshButton;

    public BenevoleListeReservationView(ReservationServiceV2 reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion des réservations");
        add(title);

        // Section de recherche
        createSearchSection();

        // Grille des membres
        membersGrid = createMembersGrid();

        // Layout pour la grille des membres avec titre
        VerticalLayout membersLayout = new VerticalLayout();
        membersLayout.add(new H3("Membres avec réservations à préparer"), membersGrid);
        membersLayout.setSizeFull();
        membersLayout.setFlexGrow(1, membersGrid);

        add(membersLayout);
        setFlexGrow(1, membersLayout);

        // Chargement initial des membres
        updateMembersList();
    }

    private void createSearchSection() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom d'utilisateur, prénom ou nom");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(500);
        searchField.addValueChangeListener(e -> updateMembersList());

        refreshButton = new Button("Actualiser");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> {
            searchField.clear();
            updateMembersList();
        });

        searchLayout.add(searchField, refreshButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private Grid<UserDto> createMembersGrid() {
        Grid<UserDto> grid = new Grid<>();
        grid.addColumn(UserDto::getUsername).setHeader("Nom d'utilisateur").setSortable(true).setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(UserDto::getFirstName).setHeader("Prénom").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(UserDto::getLastName).setHeader("Nom").setSortable(true).setAutoWidth(true).setFlexGrow(1);

        // Colonne pour le statut
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span statusBadge = new Span();
            statusBadge.getElement().getThemeList().add("badge");

            if (StatusUtils.UserStatus.ACTIVE.equals(user.getStatus())) {
                statusBadge.getElement().getThemeList().add("success");
                statusBadge.setText("Actif");
            } else {
                statusBadge.getElement().getThemeList().add("error");
                statusBadge.setText("Inactif");
            }

            return statusBadge;
        })).setHeader("Statut").setAutoWidth(true);

        // Colonne pour le nombre de réservations
        grid.addColumn(user -> {
            List<ReservationDto> reservations = reservationService
                    .findReadyForPreparationReservationsByMember(user.getId());
            return reservations.size();
        }).setHeader("Nombre").setAutoWidth(true);

        // Colonne avec bouton pour voir les réservations
        grid.addComponentColumn(user -> {
            Button viewButton = new Button("Voir réservations");
            viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            viewButton.addClickListener(e -> openReservationsDialog(user));
            return viewButton;
        }).setHeader("Actions").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("100%");

        return grid;
    }

    private void openReservationsDialog(UserDto member) {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");
        dialog.setHeight("600px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(true);

        H3 memberName = new H3("Réservations de " + member.getFirstName() + " " + member.getLastName());

        // Créer la grille des réservations
        Grid<ReservationDto> reservationsGrid = new Grid<>();

        // Ajouter la case à cocher pour marquer comme prêt
        reservationsGrid.addComponentColumn(reservation -> {
            Checkbox readyCheckbox = new Checkbox();
            readyCheckbox.setLabel("Prêt");
            readyCheckbox.setValue("ready".equals(reservation.getStatus()));

            readyCheckbox.addValueChangeListener(event -> {
                try {
                    if (event.getValue()) {
                        reservationService.markAsReady(reservation.getId());
                        showNotification("Document marqué comme prêt", "success");
                    } else {
                        reservationService.markAsNotReady(reservation.getId());
                        showNotification("Document marqué comme non prêt", "info");
                    }

                    // Mise à jour des données de la grille
                    updateReservationsGrid(reservationsGrid, member);

                    // Mettre à jour la grille des membres également
                    updateMembersList();

                } catch (Exception e) {
                    showNotification("Erreur lors de la mise à jour: " + e.getMessage(), "error");
                    readyCheckbox.setValue(!event.getValue()); // Revert
                }
            });

            return readyCheckbox;
        }).setHeader("État").setWidth("100px").setFlexGrow(0);

        // ID de la copie
        reservationsGrid.addColumn(reservation -> reservation.getCopy().getId())
                .setHeader("ID Copie")
                .setWidth("100px")
                .setFlexGrow(0);

        // Titre du document
        reservationsGrid.addColumn(reservation -> reservation.getCopy().getItem().getTitle())
                .setHeader("Titre")
                .setFlexGrow(1);

        // Type de document
        reservationsGrid.addColumn(reservation -> {
            return StatusUtils.DocTypes.toFrench(reservation.getCopy().getItem().getType());
        }).setHeader("Type").setWidth("120px").setFlexGrow(0);

        // Date de réservation
        reservationsGrid
                .addColumn(reservation -> reservation.getReservationDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de réservation")
                .setWidth("150px")
                .setFlexGrow(0);

        // Statut en texte
        reservationsGrid.addColumn(new ComponentRenderer<>(reservation -> {
            Span statusBadge = new Span();
            statusBadge.getElement().getThemeList().add("badge");

            if (StatusUtils.ReservationStatus.READY.equals(reservation.getStatus())) {
                statusBadge.getElement().getThemeList().add("success");
                statusBadge.setText("Prêt");
            } else {
                statusBadge.getElement().getThemeList().add("contrast");
                statusBadge.setText("En attente");
            }

            return statusBadge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);

        reservationsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        reservationsGrid.setHeight("400px");

        // Charger les réservations
        updateReservationsGrid(reservationsGrid, member);

        // Bouton pour fermer la boîte de dialogue
        Button closeButton = new Button("Fermer");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addClickListener(e -> dialog.close());

        dialogLayout.add(memberName, reservationsGrid, closeButton);
        dialogLayout.setFlexGrow(1, reservationsGrid);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void updateReservationsGrid(Grid<ReservationDto> grid, UserDto member) {
        List<ReservationDto> reservations = reservationService
                .findReadyForPreparationReservationsByMember(member.getId());

        grid.setItems(reservations);
    }

    private void updateMembersList() {
        List<UserDto> members;
        String searchTerm = searchField.getValue();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Obtenir tous les membres qui ont des réservations prêtes à être préparées
            members = reservationService.findDistinctMembersByStatus(StatusUtils.ReservationStatus.PENDING);
        } else {
            // Filtrer les membres par terme de recherche
            String finalSearchTerm = searchTerm.toLowerCase();
            members = reservationService.findDistinctMembersByStatus(StatusUtils.ReservationStatus.PENDING).stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(finalSearchTerm) ||
                            (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                            ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(finalSearchTerm)))
                    .toList();
        }

        // Ne montrer que les membres qui ont des réservations pouvant être préparées
        List<UserDto> filteredMembers = members.stream()
                .filter(member -> !reservationService.findReadyForPreparationReservationsByMember(member.getId())
                        .isEmpty())
                .toList();

        membersGrid.setItems(filteredMembers);

        if (filteredMembers.isEmpty()) {
            showNotification("Aucun membre n'a de réservation à préparer pour le moment", "info");
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
            default:
                // Pas de variante additionnelle
        }

        notification.open();
    }
}