package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.utils.StatusUtils;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Documents en retard")
@Route(value = "volunteer/retards", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleRetardsView extends VerticalLayout {

    private final LoanServiceV2 loanService;
    private final UserServiceV2 userService;

    // Composants UI
    private TextField searchField;
    private Grid<UserWithOverdueInfo> membersGrid;
    private Button refreshButton;

    // Classe interne pour stocker les informations des membres avec des documents
    // en retard
    private static class UserWithOverdueInfo {
        private final UserDto user;
        private final int overdueCount;
        private final int maxDaysOverdue;

        public UserWithOverdueInfo(UserDto user, int overdueCount, int maxDaysOverdue) {
            this.user = user;
            this.overdueCount = overdueCount;
            this.maxDaysOverdue = maxDaysOverdue;
        }

        public UserDto getUser() {
            return user;
        }

        public int getOverdueCount() {
            return overdueCount;
        }

        public int getMaxDaysOverdue() {
            return maxDaysOverdue;
        }
    }

    public BenevoleRetardsView(LoanServiceV2 loanService, UserServiceV2 userService) {
        this.loanService = loanService;
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion des documents en retard");
        add(title);

        // Section de recherche
        createSearchSection();

        // Grille des membres avec documents en retard
        membersGrid = createMembersGrid();

        // Layout pour la grille des membres avec titre
        VerticalLayout membersLayout = new VerticalLayout();
        membersLayout.add(new H3("Membres avec documents en retard"), membersGrid);
        membersLayout.setSizeFull();
        membersLayout.setFlexGrow(1, membersGrid);

        add(membersLayout);
        setFlexGrow(1, membersLayout);

        // Chargement initial des membres
        updateMembersList("");
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
        searchField.addValueChangeListener(e -> updateMembersList(e.getValue()));

        refreshButton = new Button("Actualiser");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> {
            searchField.clear();
            updateMembersList("");
        });

        searchLayout.add(searchField, refreshButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private Grid<UserWithOverdueInfo> createMembersGrid() {
        Grid<UserWithOverdueInfo> grid = new Grid<>();
        grid.addColumn(info -> info.getUser().getUsername()).setHeader("Nom d'utilisateur").setSortable(true);
        grid.addColumn(info -> info.getUser().getFirstName()).setHeader("Prénom").setSortable(true);
        grid.addColumn(info -> info.getUser().getLastName()).setHeader("Nom").setSortable(true);

        grid.addColumn(UserWithOverdueInfo::getOverdueCount)
                .setHeader("Documents en retard")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(UserWithOverdueInfo::getMaxDaysOverdue)
                .setHeader("Jours de retard max")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne pour le statut
        grid.addColumn(new ComponentRenderer<>(info -> {
            Span statusBadge = new Span();
            statusBadge.getElement().getThemeList().add("badge");

            // Définir la couleur en fonction du nombre de jours de retard
            if (info.getMaxDaysOverdue() > 30) {
                statusBadge.getElement().getThemeList().add("error");
                statusBadge.setText("Critique");
            } else if (info.getMaxDaysOverdue() > 14) {
                statusBadge.getElement().getThemeList().add("warning");
                statusBadge.setText("Important");
            } else {
                statusBadge.getElement().getThemeList().add("contrast");
                statusBadge.setText("Léger");
            }

            return statusBadge;
        })).setHeader("Niveau").setAutoWidth(true);

        // Colonne avec bouton pour voir les détails
        grid.addComponentColumn(info -> {
            Button viewButton = new Button("Voir détails");
            viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            viewButton.addClickListener(e -> openOverdueItemsDialog(info.getUser()));
            return viewButton;
        }).setHeader("Actions").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("100%");

        return grid;
    }

    private void openOverdueItemsDialog(UserDto member) {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");
        dialog.setHeight("600px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(true);

        H3 memberName = new H3("Documents en retard de " + member.getFirstName() + " " + member.getLastName());

        // Créer la grille des emprunts en retard
        Grid<LoanDto> overdueLoansGrid = new Grid<>();

        // ID de la copie
        overdueLoansGrid.addColumn(loan -> loan.getCopy().getId())
                .setHeader("ID Copie")
                .setWidth("100px")
                .setFlexGrow(0);

        // Titre du document
        overdueLoansGrid.addColumn(loan -> loan.getCopy().getItem().getTitle())
                .setHeader("Titre")
                .setFlexGrow(1);

        // Type de document
        overdueLoansGrid.addColumn(loan -> {
            return StatusUtils.DocTypes.toFrench(loan.getCopy().getItem().getType());
        }).setHeader("Type").setWidth("120px").setFlexGrow(0);

        // Date d'emprunt
        overdueLoansGrid
                .addColumn(loan -> loan.getLoanDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt")
                .setWidth("150px")
                .setFlexGrow(0);

        // Date de retour prévue
        overdueLoansGrid
                .addColumn(loan -> loan.getReturnDueDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de retour prévue")
                .setWidth("170px")
                .setFlexGrow(0);

        // Jours de retard
        overdueLoansGrid.addColumn(loan -> {
            LocalDate dueDate = loan.getReturnDueDate();
            long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            return daysLate;
        }).setHeader("Jours de retard").setWidth("120px").setFlexGrow(0);

        overdueLoansGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        overdueLoansGrid.setHeight("400px");

        // Charger les emprunts en retard
        List<LoanDto> overdueLoans = getOverdueLoansForMember(member.getId());
        overdueLoansGrid.setItems(overdueLoans);

        // Bouton pour fermer la boîte de dialogue
        Button closeButton = new Button("Fermer");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(memberName, overdueLoansGrid, buttonLayout);
        dialogLayout.setFlexGrow(1, overdueLoansGrid);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private List<LoanDto> getOverdueLoansForMember(Long memberId) {
        // Récupérer tous les emprunts du membre avec le statut "borrowed"
        List<LoanDto> memberLoans = loanService.findByMember(memberId).stream()
                .filter(loan -> StatusUtils.LoanStatus.BORROWED.equals(loan.getStatus()))
                .collect(Collectors.toList());

        // Filtrer pour ne garder que les emprunts en retard (date de retour < date du
        // jour)
        LocalDate today = LocalDate.now();
        return memberLoans.stream()
                .filter(loan -> loan.getReturnDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    private void updateMembersList(String searchTerm) {
        // 1. Récupérer tous les emprunts avec le statut "borrowed"
        List<LoanDto> allBorrowedLoans = loanService.findByStatus(StatusUtils.LoanStatus.BORROWED);

        // 2. Filtrer pour ne garder que les emprunts en retard
        LocalDate today = LocalDate.now();
        List<LoanDto> overdueLoans = allBorrowedLoans.stream()
                .filter(loan -> loan.getReturnDueDate().isBefore(today))
                .collect(Collectors.toList());

        // 3. Regrouper par membre et compter le nombre de retards
        Map<Long, List<LoanDto>> loansByMember = new HashMap<>();
        for (LoanDto loan : overdueLoans) {
            Long memberId = loan.getMember().getId();
            if (!loansByMember.containsKey(memberId)) {
                loansByMember.put(memberId, new ArrayList<>());
            }
            loansByMember.get(memberId).add(loan);
        }

        // 4. Créer la liste des membres avec leurs informations de retard
        List<UserWithOverdueInfo> membersWithOverdue = new ArrayList<>();
        for (Map.Entry<Long, List<LoanDto>> entry : loansByMember.entrySet()) {
            Long memberId = entry.getKey();
            List<LoanDto> memberOverdueLoans = entry.getValue();

            // Trouver le nombre de jours de retard maximum
            int maxDaysOverdue = 0;
            for (LoanDto loan : memberOverdueLoans) {
                int daysLate = (int) ChronoUnit.DAYS.between(loan.getReturnDueDate(), today);
                if (daysLate > maxDaysOverdue) {
                    maxDaysOverdue = daysLate;
                }
            }

            UserDto member = memberOverdueLoans.get(0).getMember();
            membersWithOverdue.add(new UserWithOverdueInfo(member, memberOverdueLoans.size(), maxDaysOverdue));
        }

        // 5. Filtrer par terme de recherche si nécessaire
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String finalSearchTerm = searchTerm.toLowerCase();
            membersWithOverdue = membersWithOverdue.stream()
                    .filter(info -> {
                        UserDto user = info.getUser();
                        return user.getUsername().toLowerCase().contains(finalSearchTerm) ||
                                (user.getFirstName() != null
                                        && user.getFirstName().toLowerCase().contains(finalSearchTerm))
                                ||
                                (user.getLastName() != null
                                        && user.getLastName().toLowerCase().contains(finalSearchTerm));
                    })
                    .collect(Collectors.toList());
        }

        // 6. Mettre à jour la grille
        membersGrid.setItems(membersWithOverdue);

        if (membersWithOverdue.isEmpty()) {
            showNotification("Aucun membre n'a de document en retard", "info");
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