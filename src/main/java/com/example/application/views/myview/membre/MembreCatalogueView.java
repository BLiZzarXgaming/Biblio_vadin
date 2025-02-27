package com.example.application.views.myview.membre;

import com.example.application.entity.DTO.CopyDto;
import com.example.application.entity.DTO.ItemDto;
import com.example.application.entity.DTO.LoanSettingDto;
import com.example.application.entity.DTO.ReservationDto;
import com.example.application.entity.DTO.SpecialLimitDto;
import com.example.application.entity.DTO.UserDto;
import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.objectcustom.MoisOption;
import com.example.application.security.Roles;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

@PageTitle("Catalogue des Documents")
@Route(value = "member/catalogue", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreCatalogueView extends VerticalLayout {
    private ComboBox<String> typeComboBox;
    private FormLayout searchFieldsLayout;

    private Button searchButton;
    private Button clearButton;
    private Boolean searchFieldsVisible = true;

    private Grid<ItemDto> resultsGrid;
    private CallbackDataProvider<ItemDto, Void> dataProvider;

    private ItemServiceV2 itemService;
    private PublisherServiceV2 publisherService;
    private CategoryServiceV2 categoryService;
    private SupplierServiceV2 supplierService;
    private BookServiceV2 bookService;
    private MagazineServiceV2 magazineService;
    private BoardGameServiceV2 boardGameService;
    private CopyServiceV2 copyService;
    private UserServiceV2 userService;
    private ReservationServiceV2 reservationService;
    private LoanServiceV2 loanService;
    private LoanSettingServiceV2 loanSettingService;
    private SpecialLimitService specialLimitService;

    // Pagination
    private int pageSize = 10;
    private int currentPage = 0;
    private Span paginationInfo;
    private Button prevButton;
    private Button nextButton;

    // Current user
    private UserDto currentUser;

    // Search criteria
    private String selectedType;
    private Map<String, Object> searchCriteria = new HashMap<>();

    // Search fields
    private TextField titleField;
    private TextField authorField;
    private TextField isbnField;
    private DatePicker publicationDateField;
    private ComboBox<com.example.application.entity.DTO.CategoryDto> categoryComboBox;
    private ComboBox<com.example.application.entity.DTO.PublisherDto> publisherComboBox;

    private TextField keywordField;

    private TextField isniField;
    private ComboBox<MoisOption> monthComboBox;
    private IntegerField yearField;

    private IntegerField numberOfPiecesField;
    private IntegerField recommendedAgeField;
    private TextField gtinField;

    public MembreCatalogueView(ItemServiceV2 itemService,
            PublisherServiceV2 publisherService,
            CategoryServiceV2 categoryService,
            SupplierServiceV2 supplierService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CopyServiceV2 copyService,
            UserServiceV2 userService,
            ReservationServiceV2 reservationService,
            LoanServiceV2 loanService,
            LoanSettingServiceV2 loanSettingService,
            SpecialLimitService specialLimitService) {
        this.itemService = itemService;
        this.publisherService = publisherService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.copyService = copyService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.loanService = loanService;
        this.loanSettingService = loanSettingService;
        this.specialLimitService = specialLimitService;

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<UserDto> user = userService.findByUsername(username);
        if (user.isPresent()) {
            currentUser = user.get();
        }

        setWidth("100%");
        getStyle().set("flex-grow", "1");
        setHeight("100%");

        H2 title = new H2("Catalogue des Documents");
        add(title);

        configureComponents();
    }

    private void configureComponents() {
        // Initialisation des composants
        typeComboBox = new ComboBox<>("Type de document");
        typeComboBox.setItems("Tous", "Livre", "Revue", "Jeu");
        typeComboBox.setValue("Tous");

        searchFieldsLayout = new FormLayout();
        searchFieldsLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3));

        searchButton = new Button("Rechercher", e -> searchItems());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        clearButton = new Button("Réinitialiser", e -> clearSearchFields());
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout searchActions = new HorizontalLayout(searchButton, clearButton);
        searchActions.setSpacing(true);

        add(createSearchLayout(), searchActions);
        configureGrid();
        configurePagination();
    }

    private void configurePagination() {
        paginationInfo = new Span("Page 1");

        prevButton = new Button(new Icon(VaadinIcon.ARROW_LEFT), e -> {
            if (currentPage > 0) {
                currentPage--;
                updateResults();
            }
        });
        prevButton.setEnabled(false);

        nextButton = new Button(new Icon(VaadinIcon.ARROW_RIGHT), e -> {
            currentPage++;
            updateResults();
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(prevButton, paginationInfo, nextButton);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        add(paginationLayout);
    }

    private VerticalLayout createSearchLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        // Listener pour adapter les inputs en fonction du type
        typeComboBox.addValueChangeListener(e -> updateSearchFields());

        // Initialisation des champs de recherche
        updateSearchFields();

        layout.add(typeComboBox, searchFieldsLayout);
        return layout;
    }

    private void updateCategoryComboBox() {
        List<com.example.application.entity.DTO.CategoryDto> categories = categoryService.findAll();
        categoryComboBox.setItems(categories);
        categoryComboBox.setItemLabelGenerator(com.example.application.entity.DTO.CategoryDto::getName);
    }

    private void updatePublisherComboBox() {
        List<com.example.application.entity.DTO.PublisherDto> publishers = publisherService.findAll();
        publisherComboBox.setItems(publishers);
        publisherComboBox.setItemLabelGenerator(com.example.application.entity.DTO.PublisherDto::getName);
    }

    private void updateSearchFields() {
        searchFieldsLayout.removeAll();

        selectedType = typeComboBox.getValue();

        if ("Livre".equals(selectedType)) {
            createBookSearchFields();
        } else if ("Revue".equals(selectedType)) {
            createMagazineSearchFields();
        } else if ("Jeu".equals(selectedType)) {
            createGameSearchFields();
        } else {
            createGeneralSearchFields();
        }
    }

    private void createBookSearchFields() {
        searchCriteria.clear();

        titleField = new TextField("Titre");
        authorField = new TextField("Auteur");
        isbnField = new TextField("ISBN");
        publicationDateField = new DatePicker("Date de Publication");
        categoryComboBox = new ComboBox<>("Catégorie");
        publisherComboBox = new ComboBox<>("Éditeur");

        updateCategoryComboBox();
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, authorField, isbnField, publicationDateField, categoryComboBox,
                publisherComboBox);
    }

    private void createMagazineSearchFields() {
        searchCriteria.clear();

        titleField = new TextField("Titre");
        isniField = new TextField("ISNI");
        monthComboBox = new ComboBox<>("Mois de Publication");
        yearField = new IntegerField("Année");
        categoryComboBox = new ComboBox<>("Catégorie");
        publisherComboBox = new ComboBox<>("Éditeur");

        List<MoisOption> listeDesMois = MoisOption.getListeMois();
        monthComboBox.setItems(listeDesMois);
        monthComboBox.setItemLabelGenerator(MoisOption::getNom);

        updateCategoryComboBox();
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, isniField, monthComboBox, yearField, categoryComboBox, publisherComboBox);
    }

    private void createGameSearchFields() {
        searchCriteria.clear();

        titleField = new TextField("Titre");
        numberOfPiecesField = new IntegerField("Nombre de Pièces");
        recommendedAgeField = new IntegerField("Âge Recommandé");
        gtinField = new TextField("GTIN");
        categoryComboBox = new ComboBox<>("Catégorie");
        publisherComboBox = new ComboBox<>("Éditeur");

        updateCategoryComboBox();
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, numberOfPiecesField, recommendedAgeField, gtinField, categoryComboBox,
                publisherComboBox);
    }

    private void createGeneralSearchFields() {
        searchCriteria.clear();

        keywordField = new TextField("Mot-clé");
        keywordField.setPlaceholder("Rechercher par titre, auteur, etc.");
        keywordField.setClearButtonVisible(true);

        categoryComboBox = new ComboBox<>("Catégorie");
        publisherComboBox = new ComboBox<>("Éditeur");

        updateCategoryComboBox();
        updatePublisherComboBox();

        searchFieldsLayout.add(keywordField, categoryComboBox, publisherComboBox);
    }

    private void searchItems() {
        selectedType = typeComboBox.getValue();
        searchCriteria.clear();
        currentPage = 0;

        if ("Livre".equals(selectedType)) {
            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("author", authorField.getValue());
            searchCriteria.put("isbn", isbnField.getValue());
            searchCriteria.put("publicationDate", publicationDateField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
        } else if ("Revue".equals(selectedType)) {
            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("isni", isniField.getValue());
            if (monthComboBox.getValue() != null) {
                searchCriteria.put("month", monthComboBox.getValue().getNumero());
            } else {
                searchCriteria.put("month", "");
            }
            searchCriteria.put("year", yearField != null ? yearField.getValue() : null);
            searchCriteria.put("publicationDate", null);
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
        } else if ("Jeu".equals(selectedType)) {
            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("numberOfPieces", numberOfPiecesField.getValue());
            searchCriteria.put("recommendedAge", recommendedAgeField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
            searchCriteria.put("gtin", gtinField.getValue());
        } else {
            searchCriteria.put("keyword", keywordField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
        }

        updateResults();
    }

    private void updateResults() {
        int offset = currentPage * pageSize;

        resultsGrid.setItems(query -> {
            int limit = query.getLimit();
            List<ItemDto> items = itemService.fetchItemsWithFilters(searchCriteria, selectedType, offset, limit);

            // Disable/enable pagination buttons
            prevButton.setEnabled(currentPage > 0);
            nextButton.setEnabled(items.size() == pageSize);

            // Update pagination info
            paginationInfo.setText("Page " + (currentPage + 1));

            return items.stream();
        });

        // Scroll to top of results
        resultsGrid.scrollToStart();
    }

    private void clearSearchFields() {
        typeComboBox.setValue("Tous");
        updateSearchFields();
        searchCriteria.clear();
        currentPage = 0;
        updateResults();
    }

    private String translateType(String type) {
        return switch (type) {
            case "book" -> "Livre";
            case "magazine" -> "Revue";
            case "board_game" -> "Jeu";
            default -> type;
        };
    }

    private void configureGrid() {
        resultsGrid = new Grid<>();
        resultsGrid.setSizeFull();
        resultsGrid.addClassName("catalogue-grid");

        resultsGrid.removeAllColumns();
        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        resultsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        resultsGrid.addColumn(ItemDto::getTitle).setHeader("Titre").setResizable(true).setAutoWidth(true)
                .setFlexGrow(1);
        resultsGrid.addColumn(item -> translateType(item.getType())).setHeader("Type").setResizable(true)
                .setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie").setResizable(true)
                .setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur").setResizable(true)
                .setAutoWidth(true);

        // Add availability column
        resultsGrid.addComponentColumn(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());
            long availableCount = copies.stream()
                    .filter(copy -> "available".equals(copy.getStatus()))
                    .count();

            Span badge = new Span(availableCount + " disponible(s)");
            badge.getElement().getThemeList().add("badge");

            if (availableCount > 0) {
                badge.getElement().getThemeList().add("success");
            } else {
                badge.getElement().getThemeList().add("error");
            }

            return badge;
        }).setHeader("Disponibilité").setAutoWidth(true);

        // Add view details button
        resultsGrid.addComponentColumn(item -> {
            Button detailsButton = new Button("Détails", event -> showItemDetailsDialog(item));
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            return detailsButton;
        }).setHeader("Actions").setAutoWidth(true);

        resultsGrid.setPageSize(pageSize);
        resultsGrid.setHeight("70vh");

        // Initial items load
        resultsGrid.setItems(query -> {
            int limit = query.getLimit();
            return itemService.fetchItemsWithFilters(searchCriteria, selectedType, 0, limit).stream();
        });

        add(resultsGrid);
    }

    private void showItemDetailsDialog(ItemDto selectedItem) {
        Dialog dialog = new Dialog();
        dialog.setWidth("700px");
        dialog.setHeight("auto");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        // Title with link if available
        H3 title;
        if (selectedItem.getLink() != null && !selectedItem.getLink().isEmpty()) {
            Anchor titleLink = new Anchor(selectedItem.getLink(), selectedItem.getTitle(), AnchorTarget.BLANK);
            title = new H3(titleLink);
        } else {
            title = new H3(selectedItem.getTitle());
        }
        dialogLayout.add(title);

        // Create a form layout for details
        FormLayout detailsLayout = new FormLayout();
        detailsLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2));

        // Basic information
        Span typeLabel = new Span(translateType(selectedItem.getType()));
        typeLabel.getElement().getThemeList().add("badge");

        Span categoryLabel = new Span(selectedItem.getCategory().getName());
        categoryLabel.getElement().getThemeList().add("badge");
        categoryLabel.getElement().getThemeList().add("contrast");

        detailsLayout.addFormItem(typeLabel, "Type");
        detailsLayout.addFormItem(categoryLabel, "Catégorie");
        detailsLayout.addFormItem(new Span(selectedItem.getPublisher().getName()), "Éditeur");

        // Type-specific details
        String itemType = selectedItem.getType();
        if ("book".equals(itemType)) {
            Optional<com.example.application.entity.DTO.BookDto> book = bookService.findById(selectedItem.getId());
            if (book.isPresent()) {
                detailsLayout.addFormItem(new Span(book.get().getAuthor()), "Auteur");
                detailsLayout.addFormItem(new Span(book.get().getIsbn()), "ISBN");
                detailsLayout.addFormItem(new Span(book.get().getPublicationDate().toString()), "Date de publication");
            }
        } else if ("magazine".equals(itemType)) {
            Optional<com.example.application.entity.DTO.MagazineDto> magazine = magazineService
                    .findById(selectedItem.getId());
            if (magazine.isPresent()) {
                detailsLayout.addFormItem(new Span(magazine.get().getIsni()), "ISNI");
                detailsLayout.addFormItem(new Span(magazine.get().getMonth() + " " + magazine.get().getYear()),
                        "Publication");
            }
        } else if ("board_game".equals(itemType)) {
            Optional<com.example.application.entity.DTO.BoardGameDto> boardGame = boardGameService
                    .findById(selectedItem.getId());
            if (boardGame.isPresent()) {
                detailsLayout.addFormItem(new Span(String.valueOf(boardGame.get().getNumberOfPieces())),
                        "Nombre de pièces");
                detailsLayout.addFormItem(new Span(String.valueOf(boardGame.get().getRecommendedAge())),
                        "Âge recommandé");
                detailsLayout.addFormItem(new Span(boardGame.get().getGtin()), "GTIN");

                // Game rules in a collapsible section
                Div rulesContainer = new Div();
                rulesContainer.getStyle().set("white-space", "pre-wrap");
                rulesContainer.getStyle().set("max-height", "150px");
                rulesContainer.getStyle().set("overflow-y", "auto");
                rulesContainer.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
                rulesContainer.getStyle().set("padding", "var(--lumo-space-s)");
                rulesContainer.getStyle().set("margin-top", "var(--lumo-space-s)");
                rulesContainer.setText(boardGame.get().getGameRules());

                detailsLayout.addFormItem(rulesContainer, "Règles du jeu");
            }
        }

        dialogLayout.add(detailsLayout);

        // Availability information
        List<CopyDto> copies = copyService.findByItem(selectedItem.getId());

        H4 availabilityTitle = new H4("Disponibilité");

        long totalCopies = copies.size();
        long availableCopies = copies.stream()
                .filter(copy -> "available".equals(copy.getStatus()))
                .count();

        Span availabilityInfo = new Span(availableCopies + " sur " + totalCopies + " exemplaire(s) disponible(s)");

        // Add reservation button if copies are available and user is logged in
        Button reserveButton = null;
        if (availableCopies > 0 && currentUser != null) {
            reserveButton = new Button("Réserver", e -> createReservation(selectedItem, copies));
            reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        } else if (currentUser != null) {
            // Disabled button with explanation
            reserveButton = new Button("Aucun exemplaire disponible");
            reserveButton.setEnabled(false);
        }

        VerticalLayout availabilityLayout = new VerticalLayout(availabilityTitle, availabilityInfo);
        availabilityLayout.setSpacing(false);
        availabilityLayout.setPadding(false);

        dialogLayout.add(availabilityLayout);

        if (reserveButton != null) {
            dialogLayout.add(reserveButton);
        }

        // Close button
        Button closeButton = new Button("Fermer", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogLayout.add(closeButton);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void createReservation(ItemDto item, List<CopyDto> copies) {
        // First check if the user can make more reservations
        int maxReservations = getMaxReservationsForUser();

        // Get active reservations count
        List<ReservationDto> activeReservations = reservationService.findByMember(currentUser.getId())
                .stream()
                .filter(res -> "reserved".equals(res.getStatus()) || "ready".equals(res.getStatus()))
                .toList();

        if (activeReservations.size() >= maxReservations) {
            Notification.show(
                    "Vous avez atteint votre limite de réservations (" + maxReservations + ")",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Check if the user already has a reservation for this item
        boolean alreadyReserved = activeReservations.stream()
                .anyMatch(res -> res.getCopy().getItem().getId().equals(item.getId()));

        if (alreadyReserved) {
            Notification.show(
                    "Vous avez déjà réservé ce document",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        // Get the first available copy
        Optional<CopyDto> availableCopy = copies.stream()
                .filter(copy -> "available".equals(copy.getStatus()))
                .findFirst();

        if (availableCopy.isEmpty()) {
            Notification.show(
                    "Aucun exemplaire n'est disponible pour réservation",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Confirm dialog
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmer la réservation");
        confirmDialog.setText("Souhaitez-vous réserver \"" + item.getTitle() + "\" ?");

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annuler");

        confirmDialog.setConfirmText("Réserver");
        confirmDialog.addConfirmListener(event -> {
            // Create the reservation
            ReservationDto newReservation = new ReservationDto();
            newReservation.setCopy(availableCopy.get());
            newReservation.setMember(currentUser);
            newReservation.setReservationDate(LocalDate.now());
            newReservation.setStatus("reserved");

            try {
                reservationService.save(newReservation);

                // Update copy status
                CopyDto copy = availableCopy.get();
                copy.setStatus("reserved");
                copyService.save(copy);

                Notification.show(
                        "Réservation effectuée avec succès",
                        3000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Refresh the grid to show updated availability
                updateResults();

            } catch (Exception e) {
                Notification.show(
                        "Erreur lors de la réservation: " + e.getMessage(),
                        5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmDialog.open();
    }

    private int getMaxReservationsForUser() {
        // Get default reservation limits
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);

        // Use default limits based on whether user is a child or adult
        int defaultLimit = 0;
        if (settings.isPresent()) {
            defaultLimit = Boolean.TRUE.equals(currentUser.getIsChild())
                    ? settings.get().getMaxReservationsChild()
                    : settings.get().getMaxReservationsAdult();
        } else {
            // Fallback values if settings not found
            defaultLimit = Boolean.TRUE.equals(currentUser.getIsChild()) ? 3 : 5;
        }

        // Check for special limits
        User userEntity = new User();
        userEntity.setId(currentUser.getId());

        Optional<SpecialLimitDto> specialLimit = specialLimitService.findActiveByUser(userEntity);
        if (specialLimit.isPresent()) {
            return specialLimit.get().getMaxReservations();
        }

        return defaultLimit;
    }
}