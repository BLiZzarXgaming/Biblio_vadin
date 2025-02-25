package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.LoanDto;
import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
import java.time.format.DateTimeFormatter;
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

    // Section membre
    private TextField usernameField;
    private Button checkMemberButton;
    private VerticalLayout memberInfoLayout;
    private Grid<LoanDto> activeLoansGrid;
    private Span loanLimitInfo;

    // Section recherche et emprunt
    private VerticalLayout loanProcessLayout;
    private TextField copyIdField;
    private TextField isbnField;
    private TextField gtinField;
    private TextField isniField;
    private ComboBox<MoisOption> monthField;
    private IntegerField yearField;
    private VerticalLayout searchMethodLayout;
    private VerticalLayout resultLayout;
    private Tabs searchTabs;
    private VerticalLayout copySearchLayout;
    private VerticalLayout documentSearchLayout;

    // Données de travail
    private CopyDto selectedCopy;
    private UserDto selectedMember;
    private int memberLoanLimit = 0;
    private int activeLoanCount = 0;

    // pour les reset
    private Button newMemberButton;
    private String oldUsername = "";

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

        // Section identification du membre
        createMemberSection();

        // Section recherche et emprunt (initialement cachée)
        loanProcessLayout = new VerticalLayout();
        loanProcessLayout.setVisible(false);

        // Création des composants de recherche
        createSearchSection();

        resultLayout = new VerticalLayout();
        loanProcessLayout.add(resultLayout);

        getContent().add(loanProcessLayout);

        createNewMemberButton();

    }

    private void createNewMemberButton() {
        newMemberButton = new Button("Nouveau membre");
        newMemberButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newMemberButton.setWidth("200px");
        newMemberButton.addClickListener(e -> resetView());
        newMemberButton.setVisible(false); // Initialement caché

        // Ajouter une marge pour le séparer du contenu au-dessus
        newMemberButton.getStyle()
                .set("margin-top", "30px")
                .set("margin-bottom", "20px");

        // Ajouter le bouton au bas de la vue principale
        getContent().add(newMemberButton);
    }

    private void resetView() {
        // Réinitialiser le champ du nom d'utilisateur
        usernameField.clear();

        // Cacher la section d'emprunt
        loanProcessLayout.setVisible(false);

        // Cacher la grille des emprunts actifs
        activeLoansGrid.setVisible(false);

        // Réinitialiser les informations de limite d'emprunt
        loanLimitInfo.setText("");

        // Effacer les données de recherche
        if (copyIdField != null)
            copyIdField.clear();
        if (isbnField != null)
            isbnField.clear();
        if (gtinField != null)
            gtinField.clear();
        if (isniField != null)
            isniField.clear();
        if (monthField != null)
            monthField.clear();
        if (yearField != null)
            yearField.clear();

        // Effacer la zone de résultats
        resultLayout.removeAll();

        // Réinitialiser les variables de données
        selectedCopy = null;
        selectedMember = null;
        memberLoanLimit = 0;
        activeLoanCount = 0;

        // Ajouter dans la méthode existante
        if (newMemberButton != null) {
            newMemberButton.setVisible(false);
        }
    }

    private void createMemberSection() {
        memberInfoLayout = new VerticalLayout();
        memberInfoLayout.setPadding(true);
        memberInfoLayout.setSpacing(true);

        FormLayout memberForm = new FormLayout();
        usernameField = new TextField("Nom d'utilisateur du membre");

        checkMemberButton = new Button("Vérifier membre");
        checkMemberButton.addClickListener(e -> checkMember());

        memberForm.add(usernameField, checkMemberButton);

        memberInfoLayout.add(new H3("Identification du membre"), memberForm);
        getContent().add(memberInfoLayout);

        // Grille des emprunts actifs (initialement cachée)
        activeLoansGrid = new Grid<>();
        activeLoansGrid.addColumn(loan -> loan.getCopy().getItem().getTitle()).setHeader("Titre");
        activeLoansGrid.addColumn(loan -> {
            String type = loan.getCopy().getItem().getType();
            return switch (type) {
                case "book" -> "Livre";
                case "magazine" -> "Revue";
                case "board_game" -> "Jeu";
                default -> type;
            };
        }).setHeader("Type");
        activeLoansGrid.addColumn(loan -> loan.getLoanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date d'emprunt");
        activeLoansGrid.addColumn(loan -> loan.getReturnDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date de retour prévue");
        activeLoansGrid.addComponentColumn(loan -> {
            Button cancelButton = new Button("Annuler", click -> cancelLoan(loan));
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return cancelButton;
        }).setHeader("Actions");

        activeLoansGrid.setVisible(false);
        activeLoansGrid.setHeight("250px");

        // Information sur les limites d'emprunt
        loanLimitInfo = new Span();
        loanLimitInfo.getStyle().set("font-weight", "bold");
        loanLimitInfo.getStyle().set("margin-top", "10px");

        memberInfoLayout.add(new H3("Emprunts actifs"), activeLoansGrid, loanLimitInfo);
    }

    private void createSearchSection() {
        // En-tête de la section
        H3 searchTitle = new H3("Rechercher un exemplaire");
        Span helpText = new Span("Deux méthodes sont disponibles pour trouver un exemplaire à emprunter");

        VerticalLayout headerLayout = new VerticalLayout(searchTitle, helpText);
        headerLayout.setSpacing(false);
        headerLayout.setPadding(false);

        // Onglets de recherche
        Tab copyTab = new Tab("Recherche par ID/code-barre");
        Tab documentTab = new Tab("Recherche par identifiants uniques");
        searchTabs = new Tabs(copyTab, documentTab);
        searchTabs.setWidthFull();

        // Layouts pour les deux types de recherche
        searchMethodLayout = new VerticalLayout();
        searchMethodLayout.setSpacing(true);
        searchMethodLayout.setPadding(true);

        // Préparation des deux modes de recherche
        copySearchLayout = new VerticalLayout();
        documentSearchLayout = new VerticalLayout();

        // Initialisation des deux modes
        setupCopySearchLayout();
        setupDocumentSearchLayout();

        // Par défaut, montrer la recherche par ID
        copySearchLayout.setVisible(true);
        documentSearchLayout.setVisible(false);
        searchMethodLayout.add(copySearchLayout, documentSearchLayout);

        // Listener des onglets
        searchTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(copyTab)) {
                copySearchLayout.setVisible(true);
                documentSearchLayout.setVisible(false);
            } else {
                copySearchLayout.setVisible(false);
                documentSearchLayout.setVisible(true);
            }
        });

        loanProcessLayout.add(headerLayout, searchTabs, searchMethodLayout);
    }

    private void setupCopySearchLayout() {
        Span helpText = new Span(
                "Utilisez cette option si vous avez l'ID de l'exemplaire ou si l'exemplaire possède un code-barre");
        helpText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        FormLayout formLayout = new FormLayout();
        copyIdField = new TextField("ID de la copie / Code-barre");
        copyIdField.setPlaceholder("Ex: 1001");

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchByCopyId());

        // Permettre la recherche avec la touche Entrée pour le scanning
        copyIdField.addKeyPressListener(e -> {
            if (e.getKey().equals("Enter")) {
                searchByCopyId();
            }
        });

        formLayout.add(copyIdField, searchButton);
        copySearchLayout.add(helpText, formLayout);
    }

    private void setupDocumentSearchLayout() {
        Span helpText = new Span(
                "Utilisez cette option pour trouver un exemplaire disponible en recherchant par les identifiants du document");
        helpText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Onglets pour les types de documents
        Tab bookTab = new Tab("Livre (ISBN)");
        Tab gameTab = new Tab("Jeu (GTIN)");
        Tab magazineTab = new Tab("Revue (ISNI)");

        Tabs documentTabs = new Tabs(bookTab, gameTab, magazineTab);
        documentTabs.setWidthFull();

        // Container pour les formulaires spécifiques
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setPadding(true);
        formContainer.setSpacing(true);

        // Ajouter tout au layout de recherche par document
        documentSearchLayout.add(helpText, documentTabs, formContainer);

        // Afficher le bon formulaire selon l'onglet
        documentTabs.addSelectedChangeListener(event -> {
            formContainer.removeAll();
            if (event.getSelectedTab().equals(bookTab)) {
                createBookSearchForm(formContainer);
            } else if (event.getSelectedTab().equals(gameTab)) {
                createGameSearchForm(formContainer);
            } else if (event.getSelectedTab().equals(magazineTab)) {
                createMagazineSearchForm(formContainer);
            }
        });

        // Par défaut, afficher la recherche de livre
        createBookSearchForm(formContainer);
    }

    private void createBookSearchForm(VerticalLayout container) {
        FormLayout formLayout = new FormLayout();

        isbnField = new TextField("ISBN");
        isbnField.setPlaceholder("Ex: 9780123456789");
        isbnField.setHelperText("Entrez le numéro ISBN du livre");

        Button searchBookButton = new Button("Rechercher livre");
        searchBookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBookButton.addClickListener(e -> searchBookByIsbn());

        // Permettre la recherche avec la touche Entrée
        isbnField.addKeyPressListener(e -> {
            if (e.getKey().equals("Enter")) {
                searchBookByIsbn();
            }
        });

        formLayout.add(isbnField, searchBookButton);
        container.add(formLayout);
    }

    private void createGameSearchForm(VerticalLayout container) {
        FormLayout formLayout = new FormLayout();

        gtinField = new TextField("GTIN");
        gtinField.setPlaceholder("Ex: 1234567890123");
        gtinField.setHelperText("Entrez le numéro GTIN du jeu de société");

        Button searchGameButton = new Button("Rechercher jeu");
        searchGameButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchGameButton.addClickListener(e -> searchGameByGtin());

        // Permettre la recherche avec la touche Entrée
        gtinField.addKeyPressListener(e -> {
            if (e.getKey().equals("Enter")) {
                searchGameByGtin();
            }
        });

        formLayout.add(gtinField, searchGameButton);
        container.add(formLayout);
    }

    private void createMagazineSearchForm(VerticalLayout container) {
        FormLayout formLayout = new FormLayout();

        isniField = new TextField("ISNI");
        isniField.setPlaceholder("Ex: 1234-5678");
        isniField.setHelperText("Identifiant international normalisé");

        monthField = new ComboBox<>("Mois");
        monthField.setItems(MoisOption.getListeMois());
        monthField.setItemLabelGenerator(MoisOption::getNom);

        yearField = new IntegerField("Année");
        yearField.setMin(1900);
        yearField.setMax(LocalDate.now().getYear());
        yearField.setHelperText("Année de publication");

        Button searchMagazineButton = new Button("Rechercher magazine");
        searchMagazineButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchMagazineButton.addClickListener(e -> searchMagazineByIsni());

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        formLayout.add(isniField, yearField, monthField);
        container.add(formLayout, searchMagazineButton);
    }

    private void checkMember() {
        if (loanProcessLayout.isVisible()) {
            oldUsername = usernameField.getValue();
            resetView();
            usernameField.setValue(oldUsername);
        }

        if (usernameField.isEmpty()) {
            showNotification("Veuillez saisir un nom d'utilisateur", "error");
            return;
        }

        String username = usernameField.getValue();
        Optional<UserDto> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            showNotification("Membre non trouvé", "error");
            selectedMember = null;
            loanProcessLayout.setVisible(false);
            activeLoansGrid.setVisible(false);
            return;
        }

        selectedMember = user.get();

        // Vérifier si c'est un membre (non bénévole, non admin)
        if (!"MEMBRE".equals(selectedMember.getRole().getName().toUpperCase())) {
            showNotification("L'utilisateur n'est pas un membre et ne peut pas emprunter", "error");
            selectedMember = null;
            loanProcessLayout.setVisible(false);
            activeLoansGrid.setVisible(false);
            return;
        }

        // Vérifier si le membre est actif
        if (!"active".equals(selectedMember.getStatus())) {
            showNotification("Le compte de ce membre n'est pas actif", "error");
            selectedMember = null;
            loanProcessLayout.setVisible(false);
            activeLoansGrid.setVisible(false);
            return;
        }

        // Afficher les informations du membre
        displayMemberInfo();

        // Afficher la section de recherche et création d'emprunt
        loanProcessLayout.setVisible(true);

        if (newMemberButton != null) {
            newMemberButton.setVisible(true);
        }
    }

    private void displayMemberInfo() {
        // Récupérer les emprunts actifs du membre
        List<LoanDto> activeLoans = loanService.findByMember(selectedMember.getId()).stream()
                .filter(loan -> "borrowed".equals(loan.getStatus()))
                .toList();

        activeLoanCount = activeLoans.size();

        // Récupérer les limites d'emprunt par défaut
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);

        if (settings.isEmpty()) {
            showNotification("Paramètres d'emprunt non trouvés dans le système", "error");
            return;
        }

        // Définir la limite par défaut basée sur le type de membre (enfant ou adulte)
        int defaultLimit = selectedMember.getIsChild() ? settings.get().getMaxLoansChild()
                : settings.get().getMaxLoansAdult();
        memberLoanLimit = defaultLimit;

        // Vérifier s'il existe une limite spéciale pour ce membre
        try {
            User user = new User();
            user.setId(selectedMember.getId());

            Optional<SpecialLimit> specialLimit = specialLimitService.findFirstByUserOrderByCreatedAtDesc(user);
            if (specialLimit.isPresent() && "active".equals(specialLimit.get().getStatus())) {
                memberLoanLimit = specialLimit.get().getMaxLoans();
            }
        } catch (Exception e) {
            // En cas d'erreur, on continue avec la limite par défaut
            System.err.println("Erreur lors de la récupération des limites spéciales: " + e.getMessage());
        }

        // Mettre à jour l'info sur les limites
        loanLimitInfo.setText("Emprunts actifs: " + activeLoanCount + " / " + memberLoanLimit);
        if (activeLoanCount >= memberLoanLimit) {
            loanLimitInfo.getElement().getStyle().set("color", "red");
            showNotification("Ce membre a atteint sa limite d'emprunts", "warning");
        } else {
            loanLimitInfo.getElement().getStyle().set("color", "green");
        }

        // Charger les emprunts actifs dans la grille
        activeLoansGrid.setItems(activeLoans);
        activeLoansGrid.setVisible(true);
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
        String month = monthField.getValue().getNumero();
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
            // Afficher un message clair dans l'interface
            H3 noResults = new H3("Aucun exemplaire trouvé");
            noResults.getStyle().set("color", "var(--lumo-error-color)");
            resultLayout.add(noResults);
            return;
        }

        // Afficher le titre du document
        String documentTitle = copies.get(0).getItem().getTitle();
        H3 titleHeader = new H3("Document: " + documentTitle);
        resultLayout.add(titleHeader);

        // Filtrer pour obtenir les copies disponibles
        List<CopyDto> availableCopies = copies.stream()
                .filter(copy -> "available".equals(copy.getStatus()))
                .toList();

        if (availableCopies.isEmpty()) {
            showNotification("Aucune copie disponible pour ce document", "warning");

            // Afficher un message explicatif et le nombre total de copies
            Span statusMessage = new Span("Ce document possède " + copies.size() +
                    " exemplaire(s), mais aucun n'est disponible actuellement.");
            statusMessage.getStyle().set("color", "var(--lumo-warning-color)");

            // Ajouter un récapitulatif des statuts
            long borrowedCount = copies.stream().filter(c -> "borrowed".equals(c.getStatus())).count();
            long reservedCount = copies.stream().filter(c -> "reserved".equals(c.getStatus())).count();

            StringBuilder details = new StringBuilder();
            if (borrowedCount > 0) {
                details.append(borrowedCount).append(" exemplaire(s) emprunté(s). ");
            }
            if (reservedCount > 0) {
                details.append(reservedCount).append(" exemplaire(s) réservé(s). ");
            }

            Span detailsMessage = new Span(details.toString());

            resultLayout.add(statusMessage, detailsMessage);
            return;
        }

        // Sélectionner la première copie disponible
        selectedCopy = availableCopies.get(0);
        displayCopyInfo(selectedCopy);

        // Si plusieurs copies sont disponibles, le mentionner
        if (availableCopies.size() > 1) {
            Span multipleAvailable = new Span("Note: " + (availableCopies.size() - 1) +
                    " autres exemplaires sont également disponibles");
            multipleAvailable.getStyle().set("font-style", "italic");
            resultLayout.add(multipleAvailable);
        }

        showNotification("Copie disponible trouvée (ID: " + selectedCopy.getId() + ")", "success");
    }

    private void displayCopyInfo(CopyDto copy) {
        resultLayout.removeAll();

        // En-tête avec le titre du document
        H3 titleHeader = new H3("Document: " + copy.getItem().getTitle());
        resultLayout.add(titleHeader);

        // Création d'un cadre pour les informations de la copie
        VerticalLayout copyDetailsLayout = new VerticalLayout();
        copyDetailsLayout.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        copyDetailsLayout.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        copyDetailsLayout.getStyle().set("padding", "var(--lumo-space-m)");
        copyDetailsLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        FormLayout copyInfo = new FormLayout();
        copyInfo.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // ID de la copie en gras
        TextField idField = new TextField("ID de l'exemplaire");
        idField.setValue(copy.getId().toString());
        idField.setReadOnly(true);
        idField.getStyle().set("font-weight", "bold");

        copyInfo.add(idField);

        // Ajout des autres informations
        String typeLabel = switch (copy.getItem().getType()) {
            case "book" -> "Livre";
            case "magazine" -> "Revue";
            case "board_game" -> "Jeu";
            default -> copy.getItem().getType();
        };

        TextField typeField = new TextField("Type");
        typeField.setValue(typeLabel);
        typeField.setReadOnly(true);

        TextField statusField = new TextField("Statut");
        statusField.setValue(copy.getStatus());
        statusField.setReadOnly(true);

        // Coloration du champ statut selon la disponibilité
        if ("available".equals(copy.getStatus())) {
            statusField.getStyle().set("color", "green");
        } else {
            statusField.getStyle().set("color", "red");
        }

        copyInfo.add(typeField, statusField);

        // Catégorie et éditeur si disponibles
        if (copy.getItem().getCategory() != null) {
            TextField categoryField = new TextField("Catégorie");
            categoryField.setValue(copy.getItem().getCategory().getName());
            categoryField.setReadOnly(true);
            copyInfo.add(categoryField);
        }

        if (copy.getItem().getPublisher() != null) {
            TextField publisherField = new TextField("Éditeur");
            publisherField.setValue(copy.getItem().getPublisher().getName());
            publisherField.setReadOnly(true);
            copyInfo.add(publisherField);
        }

        copyDetailsLayout.add(copyInfo);
        resultLayout.add(copyDetailsLayout);

        // Vérifier si la copie est disponible
        if (!"available".equals(copy.getStatus())) {
            Span unavailableMessage = new Span(
                    "Cet exemplaire n'est pas disponible actuellement (statut: " + copy.getStatus() + ")");
            unavailableMessage.getStyle().set("color", "var(--lumo-error-color)");
            unavailableMessage.getStyle().set("font-weight", "bold");

            // Ajouter un bouton pour chercher une autre copie disponible
            Button findAvailableCopyButton = new Button("Chercher un exemplaire disponible de ce document");
            findAvailableCopyButton.addClickListener(e -> {
                List<CopyDto> copies = copyService.findByItem(copy.getItem().getId());
                processFoundCopies(copies);
            });

            findAvailableCopyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            VerticalLayout unavailableLayout = new VerticalLayout(unavailableMessage, findAvailableCopyButton);
            unavailableLayout.setSpacing(true);
            unavailableLayout.setPadding(false);

            resultLayout.add(unavailableLayout);
            showNotification("Cette copie n'est pas disponible (statut: " + copy.getStatus() + ")", "warning");
            return;
        }

        // Vérifier si le membre a atteint sa limite d'emprunts
        if (activeLoanCount >= memberLoanLimit) {
            Span limitMessage = new Span("Le membre a atteint sa limite d'emprunts (" +
                    activeLoanCount + "/" + memberLoanLimit + ")");
            limitMessage.getStyle().set("color", "var(--lumo-error-color)");
            limitMessage.getStyle().set("font-weight", "bold");

            resultLayout.add(limitMessage);
            showNotification("Ce membre a atteint sa limite d'emprunts", "error");
            return;
        }

        // Afficher le bouton pour créer l'emprunt
        HorizontalLayout actionLayout = new HorizontalLayout();

        Button createLoanButton = new Button("Créer l'emprunt");
        createLoanButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        createLoanButton.addClickListener(e -> createLoan());

        Button cancelButton = new Button("Annuler");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> resultLayout.removeAll());

        actionLayout.add(createLoanButton, cancelButton);
        actionLayout.setSpacing(true);

        Span availableMessage = new Span("Exemplaire disponible pour l'emprunt");
        availableMessage.getStyle().set("color", "var(--lumo-success-color)");
        availableMessage.getStyle().set("font-weight", "bold");

        resultLayout.add(availableMessage, actionLayout);
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
        // TODO: Implémentation future pour la vérification des réservations

        // Vérifier que le membre n'a pas atteint sa limite
        if (activeLoanCount >= memberLoanLimit) {
            showNotification("Ce membre a atteint sa limite d'emprunts", "error");
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

            showNotification("Emprunt créé avec succès. Date de retour prévue: " +
                    savedLoan.getReturnDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "success");

            // Réinitialiser les champs de recherche de copie
            copyIdField.clear();
            if (isbnField != null)
                isbnField.clear();
            if (gtinField != null)
                gtinField.clear();
            if (isniField != null)
                isniField.clear();
            if (monthField != null)
                monthField.clear();
            if (yearField != null)
                yearField.clear();

            selectedCopy = null;
            resultLayout.removeAll();

            // Rafraîchir les informations du membre et la liste des emprunts
            displayMemberInfo();

        } catch (Exception e) {
            showNotification("Erreur lors de la création de l'emprunt: " + e.getMessage(), "error");
        }
    }

    private void cancelLoan(LoanDto loan) {
        // Vérifier que l'emprunt est bien actif
        if (!"borrowed".equals(loan.getStatus())) {
            showNotification("Cet emprunt ne peut pas être annulé car il n'est pas actif", "error");
            return;
        }

        try {
            // Mettre à jour le statut de l'emprunt
            loan.setStatus("canceled");
            loanService.save(loan);

            // Mettre à jour le statut de la copie
            CopyDto copy = loan.getCopy();
            copy.setStatus("available");
            copyService.save(copy);

            showNotification("Emprunt annulé avec succès", "success");

            // Rafraîchir les informations du membre et la liste des emprunts
            displayMemberInfo();
        } catch (Exception e) {
            showNotification("Erreur lors de l'annulation de l'emprunt: " + e.getMessage(), "error");
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
