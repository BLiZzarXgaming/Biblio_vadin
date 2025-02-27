package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.*;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@PageTitle("Catalogue des Documents")
@Route(value = "member/catalog", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreCatalogueView extends VerticalLayout {

    // Services
    private final ItemServiceV2 itemService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final CategoryServiceV2 categoryService;
    private final PublisherServiceV2 publisherService;
    private final CopyServiceV2 copyService;
    private final ReservationServiceV2 reservationService;
    private final UserServiceV2 userService;
    private final LoanSettingServiceV2 loanSettingService;
    private final SpecialLimitService specialLimitService;

    // UI Components
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<CategoryDto> categoryFilter;
    private Grid<ItemDto> resultsGrid;
    private Button searchButton;
    private Button resetButton;

    // Current user data
    private UserDto currentUser;
    private int reservationLimit = 0;
    private int currentReservations = 0;

    public MembreCatalogueView(ItemServiceV2 itemService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CategoryServiceV2 categoryService,
            PublisherServiceV2 publisherService,
            CopyServiceV2 copyService,
            ReservationServiceV2 reservationService,
            UserServiceV2 userService,
            LoanSettingServiceV2 loanSettingService,
            SpecialLimitService specialLimitService) {
        this.itemService = itemService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.categoryService = categoryService;
        this.publisherService = publisherService;
        this.copyService = copyService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.loanSettingService = loanSettingService;
        this.specialLimitService = specialLimitService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Load current user information
        loadCurrentUserInfo();

        // Create components
        createHeader();
        createSearchBar();
        createResultsGrid();

        // Initial search with empty criteria
        searchItems();
    }

    private void loadCurrentUserInfo() {
        // In a real app, get the current user from session or security context
        // This is just a placeholder implementation
        Optional<UserDto> user = userService.findByUsername(getCurrentUsername());
        if (user.isPresent()) {
            currentUser = user.get();

            // Load reservation limits
            loadReservationLimits();

            // Count current reservations
            List<ReservationDto> activeReservations = reservationService.findByMember(currentUser.getId())
                    .stream()
                    .filter(res -> "reserved".equals(res.getStatus()) || "ready".equals(res.getStatus()))
                    .toList();
            currentReservations = activeReservations.size();
        }
    }

    private String getCurrentUsername() {
        // In a real app, get username from security context
        // For demo purposes, return a sample username
        return "UsernameMembre1";
    }

    private void loadReservationLimits() {
        // Get default reservation limits
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);
        if (settings.isPresent()) {
            if (currentUser.getIsChild()) {
                reservationLimit = settings.get().getMaxReservationsChild();
            } else {
                reservationLimit = settings.get().getMaxReservationsAdult();
            }
        } else {
            // Default values if settings not found
            reservationLimit = currentUser.getIsChild() ? 3 : 5;
        }

        // Check for special limits (would use SpecialLimitService in a real
        // implementation)
        // For demo purposes, we'll just use the default limits
    }

    private void createHeader() {
        H2 title = new H2("Catalogue des Documents");
        add(title);

        // User info and limits
        if (currentUser != null) {
            HorizontalLayout userInfoLayout = new HorizontalLayout();
            userInfoLayout.setWidthFull();

            Span nameSpan = new Span("Utilisateur: " + currentUser.getFirstName() + " " + currentUser.getLastName());

            Span limitsSpan = new Span("Réservations: " + currentReservations + "/" + reservationLimit);
            limitsSpan.getStyle().set("margin-left", "auto");

            if (currentReservations >= reservationLimit) {
                limitsSpan.getStyle().set("color", "var(--lumo-error-color)");
                limitsSpan.getStyle().set("font-weight", "bold");
            } else if (currentReservations >= reservationLimit * 0.8) {
                limitsSpan.getStyle().set("color", "var(--lumo-warning-color)");
            } else {
                limitsSpan.getStyle().set("color", "var(--lumo-success-color)");
            }

            userInfoLayout.add(nameSpan, limitsSpan);
            add(userInfoLayout);
        }
    }

    private void createSearchBar() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();
        searchLayout.setAlignItems(Alignment.BASELINE);

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par titre, auteur, ISBN, etc.");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidthFull();

        // Type filter
        typeFilter = new ComboBox<>("Type");
        typeFilter.setItems("Tous", "Livre", "Revue", "Jeu");
        typeFilter.setValue("Tous");
        typeFilter.setWidth("150px");

        // Category filter
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(categoryService.findAll());
        categoryFilter.setItemLabelGenerator(CategoryDto::getName);
        categoryFilter.setWidth("200px");

        // Search button
        searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchItems());

        // Reset button
        resetButton = new Button("Réinitialiser");
        resetButton.addClickListener(e -> {
            searchField.clear();
            typeFilter.setValue("Tous");
            categoryFilter.clear();
            searchItems();
        });

        searchLayout.add(searchField, typeFilter, categoryFilter, searchButton, resetButton);
        searchLayout.setFlexGrow(1, searchField);

        add(searchLayout);
    }

    private void createResultsGrid() {
        resultsGrid = new Grid<>();
        resultsGrid.addColumn(ItemDto::getTitle).setHeader("Titre").setSortable(true).setAutoWidth(true).setFlexGrow(1);

        // Type column with human-readable values
        resultsGrid.addColumn(item -> {
            String type = item.getType();
            return switch (type) {
                case "book" -> "Livre";
                case "magazine" -> "Revue";
                case "board_game" -> "Jeu";
                default -> type;
            };
        }).setHeader("Type").setSortable(true).setAutoWidth(true);

        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie").setSortable(true)
                .setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur").setSortable(true)
                .setAutoWidth(true);

        // Availability status column
        resultsGrid.addComponentColumn(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());
            long availableCount = copies.stream().filter(c -> "available".equals(c.getStatus())).count();

            Span statusSpan = new Span();
            if (availableCount > 0) {
                statusSpan.setText(availableCount + " disponible(s)");
                statusSpan.getStyle().set("color", "var(--lumo-success-color)");
            } else {
                statusSpan.setText("Non disponible");
                statusSpan.getStyle().set("color", "var(--lumo-error-color)");
            }
            return statusSpan;
        }).setHeader("Disponibilité").setAutoWidth(true);

        // Action button column
        resultsGrid.addComponentColumn(item -> {
            Button detailsButton = new Button("Détails");
            detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            detailsButton.addClickListener(e -> showItemDetails(item));
            return detailsButton;
        }).setHeader("Actions").setAutoWidth(true);

        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        // resultsGrid.setHeightByRows(true);
        resultsGrid.setMinHeight("400px");

        // Double-click to view details
        resultsGrid.addItemDoubleClickListener(event -> showItemDetails(event.getItem()));

        add(resultsGrid);
        setFlexGrow(1, resultsGrid);
    }

    private void searchItems() {
        String searchTerm = searchField.getValue();
        String selectedType = typeFilter.getValue();
        CategoryDto selectedCategory = categoryFilter.getValue();

        // Convert UI type to backend type
        String backendType = null;
        if (!"Tous".equals(selectedType)) {
            backendType = switch (selectedType) {
                case "Livre" -> "book";
                case "Revue" -> "magazine";
                case "Jeu" -> "board_game";
                default -> null;
            };
        }

        // Build search criteria
        java.util.Map<String, Object> searchCriteria = new java.util.HashMap<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            searchCriteria.put("keyword", searchTerm);
        }

        if (selectedCategory != null) {
            searchCriteria.put("category", selectedCategory);
        }

        // Execute search
        List<ItemDto> results = itemService.fetchItemsWithFilters(searchCriteria, selectedType, 0, 1000);

        // Update grid
        resultsGrid.setItems(results);

        // Show message if no results
        if (results.isEmpty()) {
            Notification.show("Aucun résultat trouvé pour cette recherche", 3000, Notification.Position.MIDDLE);
        }
    }

    private void showItemDetails(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        // Item header with title
        H3 title = new H3(item.getTitle());

        // Basic info section
        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Type field
        String typeLabel = switch (item.getType()) {
            case "book" -> "Livre";
            case "magazine" -> "Revue";
            case "board_game" -> "Jeu";
            default -> item.getType();
        };
        TextField typeField = new TextField("Type");
        typeField.setValue(typeLabel);
        typeField.setReadOnly(true);

        // Category field
        TextField categoryField = new TextField("Catégorie");
        categoryField.setValue(item.getCategory().getName());
        categoryField.setReadOnly(true);

        // Publisher field
        TextField publisherField = new TextField("Éditeur");
        publisherField.setValue(item.getPublisher().getName());
        publisherField.setReadOnly(true);

        infoLayout.add(typeField, categoryField, publisherField);

        // Specific details based on item type
        VerticalLayout specificDetailsLayout = new VerticalLayout();
        specificDetailsLayout.setPadding(false);
        specificDetailsLayout.setSpacing(true);

        String itemType = item.getType();
        FormLayout detailsForm = new FormLayout();
        detailsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        if ("book".equals(itemType)) {
            Optional<BookDto> book = bookService.findById(item.getId());
            if (book.isPresent()) {
                BookDto bookData = book.get();

                TextField authorField = new TextField("Auteur");
                authorField.setValue(bookData.getAuthor());
                authorField.setReadOnly(true);

                TextField isbnField = new TextField("ISBN");
                isbnField.setValue(bookData.getIsbn());
                isbnField.setReadOnly(true);

                TextField pubDateField = new TextField("Date de publication");
                pubDateField.setValue(bookData.getPublicationDate().toString());
                pubDateField.setReadOnly(true);

                detailsForm.add(authorField, isbnField, pubDateField);
            }
        } else if ("magazine".equals(itemType)) {
            Optional<MagazineDto> magazine = magazineService.findById(item.getId());
            if (magazine.isPresent()) {
                MagazineDto magazineData = magazine.get();

                TextField isniField = new TextField("ISNI");
                isniField.setValue(magazineData.getIsni());
                isniField.setReadOnly(true);

                TextField issueField = new TextField("Numéro");
                issueField.setValue(magazineData.getMonth() + " " + magazineData.getYear());
                issueField.setReadOnly(true);

                TextField pubDateField = new TextField("Date de publication");
                pubDateField.setValue(magazineData.getPublicationDate().toString());
                pubDateField.setReadOnly(true);

                detailsForm.add(isniField, issueField, pubDateField);
            }
        } else if ("board_game".equals(itemType)) {
            Optional<BoardGameDto> boardGame = boardGameService.findById(item.getId());
            if (boardGame.isPresent()) {
                BoardGameDto gameData = boardGame.get();

                IntegerField piecesField = new IntegerField("Nombre de pièces");
                piecesField.setValue(gameData.getNumberOfPieces());
                piecesField.setReadOnly(true);

                IntegerField ageField = new IntegerField("Âge recommandé");
                ageField.setValue(gameData.getRecommendedAge());
                ageField.setReadOnly(true);

                TextField gtinField = new TextField("GTIN");
                gtinField.setValue(gameData.getGtin());
                gtinField.setReadOnly(true);

                detailsForm.add(piecesField, ageField, gtinField);

                // Game rules in separate section
                H4 rulesTitle = new H4("Règles du jeu");
                Paragraph rules = new Paragraph(gameData.getGameRules());
                specificDetailsLayout.add(rulesTitle, rules);
            }
        }

        specificDetailsLayout.addComponentAsFirst(detailsForm);

        // Availability section
        H4 availabilityTitle = new H4("Disponibilité");

        List<CopyDto> copies = copyService.findByItem(item.getId());
        long totalCopies = copies.size();
        long availableCopies = copies.stream().filter(copy -> "available".equals(copy.getStatus())).count();

        Paragraph availabilityInfo = new Paragraph(
                "Exemplaires disponibles: " + availableCopies + " sur " + totalCopies + " au total");

        // Reserve button
        Button reserveButton = new Button("Réserver");
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        if (availableCopies > 0) {
            // Check if user has reached reservation limit
            if (currentReservations >= reservationLimit) {
                reserveButton.setEnabled(false);
                reserveButton.getElement().setAttribute("title", "Vous avez atteint votre limite de réservations");

                Paragraph limitWarning = new Paragraph("Vous avez atteint votre limite de réservations (" +
                        currentReservations + "/" + reservationLimit + ")");
                limitWarning.getStyle().set("color", "var(--lumo-error-color)");
                specificDetailsLayout.add(limitWarning);
            } else {
                reserveButton.setEnabled(true);
                reserveButton.addClickListener(e -> createReservation(item, dialog));
            }
        } else {
            reserveButton.setEnabled(false);
            reserveButton.getElement().setAttribute("title", "Aucun exemplaire disponible");
        }

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(reserveButton, closeButton);
        buttonsLayout.setSpacing(true);

        // Add all components to dialog
        dialogLayout.add(
                title,
                infoLayout,
                new Hr(),
                specificDetailsLayout,
                new Hr(),
                availabilityTitle,
                availabilityInfo,
                buttonsLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void createReservation(ItemDto item, Dialog detailsDialog) {
        // Check if user can make reservations
        if (currentReservations >= reservationLimit) {
            Notification.show(
                    "Vous avez atteint votre limite de réservations",
                    5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Find available copy
        List<CopyDto> copies = copyService.findByItem(item.getId());
        Optional<CopyDto> availableCopy = copies.stream()
                .filter(copy -> "available".equals(copy.getStatus()))
                .findFirst();

        if (availableCopy.isEmpty()) {
            Notification.show(
                    "Désolé, ce document n'est plus disponible",
                    5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Create confirmation dialog
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmer la réservation");

        VerticalLayout confirmLayout = new VerticalLayout();
        confirmLayout.add(new Paragraph("Voulez-vous réserver le document \"" + item.getTitle() + "\" ?"));

        Button confirmButton = new Button("Confirmer");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(e -> {
            // Create reservation
            ReservationDto reservation = new ReservationDto();
            reservation.setCopy(availableCopy.get());
            reservation.setMember(currentUser);
            reservation.setReservationDate(LocalDate.now());
            reservation.setStatus("reserved");

            try {
                reservationService.save(reservation);

                // Update copy status
                CopyDto copy = availableCopy.get();
                copy.setStatus("reserved");
                copyService.save(copy);

                // Update user's reservation count
                currentReservations++;

                // Show success message
                Notification.show(
                        "Réservation effectuée avec succès !",
                        5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Update UI
                loadCurrentUserInfo();

                // Close dialogs
                confirmDialog.close();
                detailsDialog.close();

                // Refresh search results
                searchItems();

            } catch (Exception ex) {
                Notification.show(
                        "Erreur lors de la réservation: " + ex.getMessage(),
                        5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setSpacing(true);

        confirmLayout.add(buttonLayout);
        confirmDialog.add(confirmLayout);

        confirmDialog.open();
    }
}