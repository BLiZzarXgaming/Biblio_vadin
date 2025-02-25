package com.example.application.views.myview.benevole;

import com.example.application.entity.Copy;
import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.objectcustom.MoisOption;
import com.example.application.entity.Loan;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@PageTitle("Emprunts")
@Route(value = "volunteer/loan", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleEmpruntView extends Composite<VerticalLayout> {

    private final CopyServiceV2 copyService;
    private final UserServiceV2 userService;
    private final LoanServiceV2 loanService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final LoanSettingServiceV2 loanSettingService;
    private final SpecialLimitService specialLimitService;

    private TextField copyIdField;
    private TextField isbnField;
    private TextField gtinField;
    private TextField isniField;
    private ComboBox<MoisOption> monthField;
    private IntegerField yearField;
    private TextField usernameField;

    private VerticalLayout searchMethodLayout;
    private VerticalLayout resultLayout;

    private CopyDto selectedCopy;
    private UserDto selectedMember;
    private int memberLoanLimit = 0;

    public BenevoleEmpruntView(CopyServiceV2 copyService,
            UserServiceV2 userService,
            LoanServiceV2 loanService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            LoanSettingServiceV2 loanSettingService,
            SpecialLimitService specialLimitService) {
        this.copyService = copyService;
        this.userService = userService;
        this.loanService = loanService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.loanSettingService = loanSettingService;
        this.specialLimitService = specialLimitService;
        getContent().setWidth("100%");
        getContent().setHeightFull();

        H2 title = new H2("Gestion des emprunts");
        getContent().add(title);

        createSearchTabs();
        createMemberSection();

        resultLayout = new VerticalLayout();
        getContent().add(resultLayout);
    }

    private void createSearchTabs() {
        Tab copyTab = new Tab("Recherche par ID de copie");
        Tab documentTab = new Tab("Recherche par document");

        Tabs tabs = new Tabs(copyTab, documentTab);

        searchMethodLayout = new VerticalLayout();
        searchMethodLayout.setSpacing(true);
        searchMethodLayout.setPadding(true);

        tabs.addSelectedChangeListener(event -> {
            searchMethodLayout.removeAll();
            if (event.getSelectedTab().equals(copyTab)) {
                createCopySearchForm();
            } else {
                createDocumentSearchForm();
            }
        });

        // Par défaut, afficher la recherche par ID
        createCopySearchForm();

        getContent().add(tabs, searchMethodLayout);
    }

    private void createCopySearchForm() {
        FormLayout formLayout = new FormLayout();

        copyIdField = new TextField("ID de la copie");

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchByCopyId());

        formLayout.add(copyIdField, searchButton);
        searchMethodLayout.add(formLayout);
    }

    private void createDocumentSearchForm() {
        FormLayout formLayout = new FormLayout();

        isbnField = new TextField("ISBN (Livre)");
        gtinField = new TextField("GTIN (Jeu)");
        isniField = new TextField("ISNI (Magazine)");
        monthField = new ComboBox<>("Mois (Magazine)");
        monthField.setItems(MoisOption.getListeMois());
        monthField.setItemLabelGenerator(MoisOption::getNom);
        yearField = new IntegerField("Année (Magazine)");
        yearField.setMin(1900);
        yearField.setMax(LocalDate.now().getYear());

        Button searchBookButton = new Button("Rechercher livre");
        searchBookButton.addClickListener(e -> searchBookByIsbn());

        Button searchGameButton = new Button("Rechercher jeu");
        searchGameButton.addClickListener(e -> searchGameByGtin());

        Button searchMagazineButton = new Button("Rechercher magazine");
        searchMagazineButton.addClickListener(e -> searchMagazineByIsni());

        formLayout.add(isbnField, searchBookButton, gtinField, searchGameButton);

        // Ajout des champs spécifiques pour le magazine dans une sous-section
        HorizontalLayout magazineLayout = new HorizontalLayout(isniField, monthField, yearField);
        magazineLayout.setWidthFull();
        formLayout.add(magazineLayout, searchMagazineButton);

        searchMethodLayout.add(formLayout);
    }

    private void createMemberSection() {
        FormLayout memberForm = new FormLayout();

        usernameField = new TextField("Nom d'utilisateur du membre");

        Button checkMemberButton = new Button("Vérifier membre");
        checkMemberButton.addClickListener(e -> checkMember());

        memberForm.add(usernameField, checkMemberButton);
        getContent().add(memberForm);
    }

    private void searchByCopyId() {
        if (copyIdField.isEmpty()) {
            showNotification("Veuillez saisir un ID de copie", "error");
            return;
        }

        try {
            Long copyId = Long.parseLong(copyIdField.getValue());
            Optional<CopyDto> copy = copyService.findById(copyId);

            if (copy.isPresent()) {
                selectedCopy = copy.get();
                displayCopyInfo(selectedCopy);
            } else {
                showNotification("Aucune copie trouvée avec cet ID", "error");
                selectedCopy = null;
                resultLayout.removeAll();
            }
        } catch (NumberFormatException e) {
            showNotification("ID de copie invalide", "error");
        }
    }

    private void searchBookByIsbn() {
        if (isbnField.isEmpty()) {
            showNotification("Veuillez saisir un ISBN", "error");
            return;
        }

        String isbn = isbnField.getValue();
        bookService.findByIsbn(isbn).ifPresentOrElse(
                bookDto -> {
                    List<CopyDto> copies = copyService.findByItem(bookDto.getItem().getId());
                    processFoundCopies(copies);
                },
                () -> showNotification("Aucun livre trouvé avec cet ISBN", "error"));
    }

    private void searchGameByGtin() {
        if (gtinField.isEmpty()) {
            showNotification("Veuillez saisir un GTIN", "error");
            return;
        }

        String gtin = gtinField.getValue();
        boardGameService.findByGtin(gtin).ifPresentOrElse(
                gameDto -> {
                    List<CopyDto> copies = copyService.findByItem(gameDto.getItem().getId());
                    processFoundCopies(copies);
                },
                () -> showNotification("Aucun jeu trouvé avec ce GTIN", "error"));
    }

    private void searchMagazineByIsni() {
        if (isniField.isEmpty() || monthField.isEmpty() || yearField.isEmpty()) {
            showNotification("Veuillez saisir l'ISNI, le mois et l'année du magazine", "error");
            return;
        }

        String isni = isniField.getValue();
        String month = monthField.getValue().getNom();
        String year = yearField.getValue().toString();

        magazineService.findByIsniAndMonthAndYear(isni, month, year).ifPresentOrElse(
                magazineDto -> {
                    List<CopyDto> copies = copyService.findByItem(magazineDto.getItem().getId());
                    processFoundCopies(copies);
                },
                () -> showNotification("Aucun magazine trouvé avec cet ISNI, mois et année", "error"));
    }

    private void processFoundCopies(List<CopyDto> copies) {
        resultLayout.removeAll();

        if (copies.isEmpty()) {
            showNotification("Aucune copie trouvée pour ce document", "error");
            return;
        }

        // Filtrer pour obtenir les copies disponibles
        List<CopyDto> availableCopies = copies.stream()
                .filter(copy -> "available".equals(copy.getStatus()))
                .toList();

        if (availableCopies.isEmpty()) {
            showNotification("Aucune copie disponible pour ce document", "warning");
            resultLayout.add(new H2("Toutes les copies sont indisponibles"));
            return;
        }

        // Sélectionner la première copie disponible
        selectedCopy = availableCopies.get(0);
        displayCopyInfo(selectedCopy);

        showNotification("Copie disponible trouvée (ID: " + selectedCopy.getId() + ")", "success");
    }

    private void displayCopyInfo(CopyDto copy) {
        resultLayout.removeAll();

        FormLayout copyInfo = new FormLayout();

        copyInfo.addFormItem(new TextField("ID", copy.getId().toString()), "ID de copie");
        copyInfo.addFormItem(new TextField("Titre", copy.getItem().getTitle()), "Titre");
        copyInfo.addFormItem(new TextField("Type", copy.getItem().getType()), "Type");
        copyInfo.addFormItem(new TextField("Statut", copy.getStatus()), "Statut");

        // Vérifier si la copie est disponible
        if (!"available".equals(copy.getStatus())) {
            showNotification("Cette copie n'est pas disponible (statut: " + copy.getStatus() + ")", "warning");

            // Ajouter un bouton pour chercher une autre copie disponible
            Button findAvailableCopyButton = new Button("Chercher une copie disponible de ce document");
            findAvailableCopyButton.addClickListener(e -> {
                List<CopyDto> copies = copyService.findByItem(copy.getItem().getId());
                processFoundCopies(copies);
            });

            resultLayout.add(copyInfo, findAvailableCopyButton);
            return;
        }

        Button createLoanButton = new Button("Créer l'emprunt");
        createLoanButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createLoanButton.addClickListener(e -> createLoan());

        resultLayout.add(copyInfo, createLoanButton);
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
            selectedMember = null;
            return;
        }

        selectedMember = user.get();

        // Vérifier si c'est un membre (non bénévole, non admin)
        if (!"MEMBRE".equals(selectedMember.getRole().getName())) {
            showNotification("L'utilisateur n'est pas un membre et ne peut pas emprunter", "error");
            selectedMember = null;
            return;
        }

        // Vérifier si le membre est actif
        if (!"active".equals(selectedMember.getStatus())) {
            showNotification("Le compte de ce membre n'est pas actif", "error");
            selectedMember = null;
            return;
        }

        // Vérifier les limites d'emprunt
        checkLoanLimits();
    }

    private void checkLoanLimits() {
        if (selectedMember == null) {
            return;
        }

        // Récupérer les emprunts actifs du membre
        List<LoanDto> activeLoans = loanService.findByMember(selectedMember.getId()).stream()
                .filter(loan -> "borrowed".equals(loan.getStatus()))
                .toList();

        // Récupérer les paramètres de prêt
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L); // Assumer que les paramètres ont l'ID 1

        if (settings.isEmpty()) {
            showNotification("Paramètres d'emprunt non trouvés dans le système", "error");
            return;
        }

        int maxLoans = selectedMember.getIsChild() ? settings.get().getMaxLoansChild()
                : settings.get().getMaxLoansAdult();

        if (activeLoans.size() >= maxLoans) {
            showNotification("Ce membre a atteint sa limite d'emprunts (" + activeLoans.size() + "/" + maxLoans + ")",
                    "error");
            return;
        }

        showNotification("Le membre peut emprunter (" + activeLoans.size() + "/" + maxLoans + " emprunts actifs)",
                "success");
    }

    private void createLoan() {
        // Vérifier que tout est prêt pour l'emprunt
        if (selectedCopy == null) {
            showNotification("Aucune copie sélectionnée", "error");
            return;
        }

        if (selectedMember == null) {
            showNotification("Aucun membre sélectionné", "error");
            return;
        }

        if (!"available".equals(selectedCopy.getStatus())) {
            showNotification("Cette copie n'est pas disponible", "error");
            return;
        }

        // Vérifier les réservations existantes pour cette copie
        List<LoanDto> existingLoans = loanService.findByCopy(selectedCopy.getId());
        boolean isAlreadyBorrowed = existingLoans.stream()
                .anyMatch(loan -> "borrowed".equals(loan.getStatus()));

        if (isAlreadyBorrowed) {
            showNotification("Cette copie est déjà empruntée", "error");
            return;
        }

        // Créer un nouvel emprunt
        LoanDto newLoan = new LoanDto();
        newLoan.setCopy(selectedCopy);
        newLoan.setMember(selectedMember);
        newLoan.setLoanDate(LocalDate.now());

        // Récupérer la durée du prêt depuis les paramètres
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);
        int loanDuration = settings.isPresent() ? settings.get().getLoanDurationDays() : 21; // 21 jours par défaut

        newLoan.setReturnDueDate(LocalDate.now().plusDays(loanDuration));
        newLoan.setStatus("borrowed");

        try {
            // Enregistrer l'emprunt
            LoanDto savedLoan = loanService.save(newLoan);

            // Mettre à jour le statut de la copie
            selectedCopy.setStatus("borrowed");
            copyService.save(selectedCopy);

            showNotification("Emprunt créé avec succès. Date de retour prévue: " + savedLoan.getReturnDueDate(),
                    "success");

            // Réinitialiser les champs
            copyIdField.clear();
            if (isbnField != null)
                isbnField.clear();
            if (gtinField != null)
                gtinField.clear();
            if (isniField != null)
                isniField.clear();
            usernameField.clear();

            selectedCopy = null;
            selectedMember = null;
            resultLayout.removeAll();

        } catch (Exception e) {
            showNotification("Erreur lors de la création de l'emprunt: " + e.getMessage(), "error");
        }
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
            default:
                // Aucune variante supplémentaire
        }

        notification.open();
    }
}
