package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.service.implementation.CopyServiceV2;
import com.example.application.service.implementation.LoanServiceV2;
import com.example.application.service.implementation.UserServiceV2;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Retours")
@Route(value = "volunteer/return", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleRetourView extends Composite<VerticalLayout> {

    private final LoanServiceV2 loanService;
    private final UserServiceV2 userService;
    private final CopyServiceV2 copyService;

    // Section membre
    private TextField usernameField;
    private Button checkMemberButton;
    private VerticalLayout memberInfoLayout;

    // Section scan
    private TextField scanField;

    // Section retours
    private Grid<LoanDto> activeBorrowsGrid;
    private List<LoanDto> selectedLoansForReturn = new ArrayList<>();
    private Button processReturnButton;
    private Button resetButton;

    // Données de travail
    private UserDto selectedMember;
    private List<LoanDto> activeLoans;

    public BenevoleRetourView(LoanServiceV2 loanService, UserServiceV2 userService, CopyServiceV2 copyService) {
        this.loanService = loanService;
        this.userService = userService;
        this.copyService = copyService;

        getContent().setWidth("100%");
        getContent().setHeightFull();

        H2 title = new H2("Gestion des retours");
        getContent().add(title);

        // Section identification du membre
        createMemberSection();

        // Section scan de code-barre
        createScanSection();

        // Section liste des emprunts à retourner
        createReturnSection();

        // Section boutons d'action
        createActionSection();
    }

    private void createMemberSection() {
        memberInfoLayout = new VerticalLayout();
        memberInfoLayout.setPadding(true);
        memberInfoLayout.setSpacing(true);

        FormLayout memberForm = new FormLayout();
        usernameField = new TextField("Nom d'utilisateur du membre");
        usernameField.setPlaceholder("Ex: UsernameMembre1");

        checkMemberButton = new Button("Rechercher");
        checkMemberButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        checkMemberButton.addClickListener(e -> checkMember());

        // Permettre la recherche avec la touche Entrée
        usernameField.addKeyPressListener(e -> {
            if (e.getKey().equals(Key.ENTER)) {
                checkMember();
            }
        });

        memberForm.add(usernameField, checkMemberButton);

        memberInfoLayout.add(new H3("Identification du membre"), memberForm);
        getContent().add(memberInfoLayout);
    }

    private void createScanSection() {
        VerticalLayout scanLayout = new VerticalLayout();
        scanLayout.setPadding(true);
        scanLayout.setSpacing(true);

        scanField = new TextField("Scanner un code-barre");
        scanField.setPlaceholder("Scannez ou saisissez l'ID de la copie");
        scanField.setHelperText("Appuyez sur Entrée après avoir scanné");
        scanField.setWidth("300px");

        // Ajouter l'écouteur pour la touche Entrée
        scanField.addKeyPressListener(e -> {
            if (e.getKey().equals(Key.ENTER)) {
                processScan();
            }
        });

        // Bouton de scan manuel (en plus de la touche Entrée)
        Button scanButton = new Button("Valider");
        scanButton.addClickListener(e -> processScan());

        HorizontalLayout scanInputLayout = new HorizontalLayout(scanField, scanButton);
        scanInputLayout.setAlignItems(Alignment.BASELINE);

        scanLayout.add(new H3("Scan des documents"), scanInputLayout);
        scanLayout.setVisible(false); // Caché initialement

        getContent().add(scanLayout);
    }

    private void createReturnSection() {
        VerticalLayout returnsLayout = new VerticalLayout();
        returnsLayout.setPadding(true);
        returnsLayout.setSpacing(true);

        // Grille des emprunts actifs
        activeBorrowsGrid = new Grid<>();
        activeBorrowsGrid.addComponentColumn(this::createReturnCheckbox).setHeader("Retourné")
                .setAutoWidth(true).setFlexGrow(0);
        activeBorrowsGrid.addColumn(loan -> loan.getCopy().getId()).setHeader("ID Copie")
                .setAutoWidth(true).setFlexGrow(0);
        activeBorrowsGrid.addColumn(loan -> loan.getCopy().getItem().getTitle()).setHeader("Titre")
                .setAutoWidth(true).setFlexGrow(1);
        activeBorrowsGrid.addColumn(loan -> {
            String type = loan.getCopy().getItem().getType();
            return switch (type) {
                case "book" -> "Livre";
                case "magazine" -> "Revue";
                case "board_game" -> "Jeu";
                default -> type;
            };
        }).setHeader("Type").setAutoWidth(true).setFlexGrow(0);
        activeBorrowsGrid.addColumn(loan -> loan.getLoanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt").setAutoWidth(true).setFlexGrow(0);
        activeBorrowsGrid.addColumn(loan -> loan.getReturnDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de retour prévue").setAutoWidth(true).setFlexGrow(0);
        activeBorrowsGrid.addComponentColumn(loan -> {
            Span status = new Span();
            if (loan.getReturnDueDate().isBefore(LocalDate.now())) {
                status.setText("En retard");
                status.getStyle().set("color", "red");
                status.getStyle().set("font-weight", "bold");
            } else {
                LocalDate warningDate = LocalDate.now().plusDays(3);
                if (loan.getReturnDueDate().isBefore(warningDate)) {
                    status.setText("Bientôt dû");
                    status.getStyle().set("color", "orange");
                } else {
                    status.setText("Dans les délais");
                    status.getStyle().set("color", "green");
                }
            }
            return status;
        }).setHeader("Statut").setAutoWidth(true).setFlexGrow(0);

        activeBorrowsGrid.setHeight("350px");
        activeBorrowsGrid.setVisible(false); // Caché initialement

        returnsLayout.add(new H3("Documents à retourner"), activeBorrowsGrid);
        getContent().add(returnsLayout);
    }

    private void createActionSection() {
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setPadding(true);
        actionLayout.setSpacing(true);

        processReturnButton = new Button("Finaliser les retours");
        processReturnButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        processReturnButton.addClickListener(e -> processReturns());
        processReturnButton.setVisible(false); // Caché initialement

        resetButton = new Button("Nouveau membre");
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> resetView());
        resetButton.setVisible(false); // Caché initialement

        actionLayout.add(processReturnButton, resetButton);
        getContent().add(actionLayout);
    }

    private Checkbox createReturnCheckbox(LoanDto loan) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(selectedLoansForReturn.contains(loan));
        checkbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                if (!selectedLoansForReturn.contains(loan)) {
                    selectedLoansForReturn.add(loan);
                }
            } else {
                selectedLoansForReturn.remove(loan);
            }
            updateProcessButtonState();
        });
        return checkbox;
    }

    private void updateProcessButtonState() {
        processReturnButton.setEnabled(!selectedLoansForReturn.isEmpty());
    }

    private void checkMember() {
        if (usernameField.isEmpty()) {
            showNotification("Veuillez saisir un nom d'utilisateur", "error");
            return;
        }

        String username = usernameField.getValue();
        Optional<UserDto> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            showNotification("Membre non trouvé", "error");
            resetView();
            return;
        }

        selectedMember = user.get();

        // Vérifier si c'est un membre
        if (!"MEMBRE".equals(selectedMember.getRole().getName().toUpperCase())) {
            showNotification("L'utilisateur n'est pas un membre", "error");
            resetView();
            return;
        }

        // Récupérer les emprunts actifs du membre
        activeLoans = loanService.findByMember(selectedMember.getId()).stream()
                .filter(loan -> "borrowed".equals(loan.getStatus()))
                .toList();

        if (activeLoans.isEmpty()) {
            showNotification("Ce membre n'a aucun emprunt actif", "warning");
            resetView();
            return;
        }

        // Afficher les emprunts actifs et activer la section scan
        activeBorrowsGrid.setItems(activeLoans);
        activeBorrowsGrid.setVisible(true);

        // Rendre visibles les autres sections
        getContent().getChildren().forEach(component -> {
            if (component instanceof VerticalLayout && !component.equals(memberInfoLayout)) {
                component.setVisible(true);
            }
        });

        // Activer les boutons d'action
        processReturnButton.setVisible(true);
        processReturnButton.setEnabled(false); // Initialement désactivé jusqu'à sélection
        resetButton.setVisible(true);

        // Mettre le focus sur le champ de scan
        scanField.focus();

        showNotification("Membre trouvé avec " + activeLoans.size() + " emprunts actifs", "success");
    }

    private void processScan() {
        if (scanField.isEmpty()) {
            return; // Ignorer les scans vides
        }

        if (activeLoans == null || activeLoans.isEmpty()) {
            showNotification("Veuillez d'abord sélectionner un membre", "error");
            scanField.clear();
            return;
        }

        try {
            Long copyId = Long.parseLong(scanField.getValue());

            // Chercher l'emprunt correspondant à cette copie
            Optional<LoanDto> matchingLoan = activeLoans.stream()
                    .filter(loan -> loan.getCopy().getId().equals(copyId))
                    .findFirst();

            if (matchingLoan.isPresent()) {
                LoanDto loan = matchingLoan.get();

                // Ajouter ou retirer de la sélection
                if (selectedLoansForReturn.contains(loan)) {
                    selectedLoansForReturn.remove(loan);
                    showNotification("Document retiré de la sélection", "info");
                } else {
                    selectedLoansForReturn.add(loan);
                    showNotification("Document marqué pour retour", "success");
                }

                // Rafraîchir la grille pour mettre à jour les cases à cocher
                activeBorrowsGrid.getDataProvider().refreshAll();
                updateProcessButtonState();
            } else {
                showNotification("Aucun emprunt trouvé pour cette copie (ID: " + copyId + ")", "error");
            }
        } catch (NumberFormatException e) {
            showNotification("ID de copie invalide", "error");
        }

        // Vider et redonner le focus au champ de scan
        scanField.clear();
        scanField.focus();
    }

    private void processReturns() {
        if (selectedLoansForReturn.isEmpty()) {
            showNotification("Aucun document sélectionné pour le retour", "warning");
            return;
        }

        int successCount = 0;
        int errorCount = 0;

        for (LoanDto loan : selectedLoansForReturn) {
            try {
                // Mettre à jour le statut de l'emprunt
                loan.setStatus("returned");
                loanService.save(loan);

                // Mettre à jour le statut de la copie
                CopyDto copy = loan.getCopy();
                copy.setStatus("available");
                copyService.save(copy);

                successCount++;
            } catch (Exception e) {
                errorCount++;
                System.err.println("Erreur lors du traitement du retour pour l'emprunt ID: " + loan.getId());
                e.printStackTrace();
            }
        }

        // Afficher un récapitulatif des opérations
        if (errorCount == 0) {
            showNotification(successCount + " document(s) retourné(s) avec succès", "success");
        } else {
            showNotification(successCount + " document(s) retourné(s) avec succès, " +
                    errorCount + " erreur(s)", "warning");
        }

        // Réinitialiser la vue après traitement
        resetView();
    }

    private void resetView() {
        // Réinitialiser les champs et les données
        usernameField.clear();
        scanField.clear();
        selectedLoansForReturn.clear();
        selectedMember = null;
        activeLoans = null;

        // Cacher les sections non nécessaires
        activeBorrowsGrid.setVisible(false);
        processReturnButton.setVisible(false);
        resetButton.setVisible(false);

        // Cacher les sections de scan et de retour
        getContent().getChildren().forEach(component -> {
            if (component instanceof VerticalLayout && !component.equals(memberInfoLayout)) {
                component.setVisible(false);
            }
        });

        // Redonner le focus au champ de nom d'utilisateur
        usernameField.focus();
    }

    private void showNotification(String message, String type) {
        Notification notification = new Notification(message);
        notification.setDuration(5000);

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
                // Aucune variante supplémentaire
        }

        notification.open();
    }
}