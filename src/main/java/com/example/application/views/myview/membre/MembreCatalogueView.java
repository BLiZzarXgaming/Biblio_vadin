package com.example.application.views.myview.membre;

import com.example.application.entity.SpecialLimit;
import com.example.application.entity.User;
import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.MoisOption;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Catalogue")
@Route(value = "member/catalogue", layout = MainLayout.class)
@RolesAllowed("ROLE_MEMBRE")
public class MembreCatalogueView extends VerticalLayout {

    private final ItemServiceV2 itemService;
    private final CopyServiceV2 copyService;
    private final ReservationServiceV2 reservationService;
    private final LoanServiceV2 loanService;
    private final LoanSettingServiceV2 loanSettingService;
    private final SpecialLimitService specialLimitService;
    private final UserServiceV2 userService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;

    // Search components
    private ComboBox<String> typeComboBox;
    private FormLayout searchFieldsLayout;
    private Button searchButton;
    private Button resetButton;
    private VerticalLayout searchLayout;

    // Grid and pagination components
    private Grid<ItemDto> resultsGrid;
    private HorizontalLayout paginationLayout;
    private Button prevButton;
    private Button nextButton;
    private Span pageInfoLabel;
    private final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int totalItems = 0;

    // Search fields
    private TextField titleField;
    private TextField authorField;
    private TextField isbnField;
    private DatePicker publicationDateField;
    private ComboBox<CategoryDto> categoryComboBox;
    private ComboBox<PublisherDto> publisherComboBox;
    private TextField keywordField;
    private TextField isniField;
    private ComboBox<MoisOption> monthComboBox;
    private IntegerField yearField;
    private TextField gtinField;
    private IntegerField numberOfPiecesField;
    private IntegerField recommendedAgeField;

    // Current user and reservation limits
    private UserDto currentUser;
    private int reservationLimit = 0;
    private int activeReservationCount = 0;

    // Current search criteria
    private String selectedType = "Tous";
    private Map<String, Object> searchCriteria = new HashMap<>();

    public MembreCatalogueView(
            ItemServiceV2 itemService,
            CopyServiceV2 copyService,
            ReservationServiceV2 reservationService,
            LoanServiceV2 loanService,
            LoanSettingServiceV2 loanSettingService,
            SpecialLimitService specialLimitService,
            UserServiceV2 userService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CategoryServiceV2 categoryService,
            PublisherServiceV2 publisherService) {
        this.itemService = itemService;
        this.copyService = copyService;
        this.reservationService = reservationService;
        this.loanService = loanService;
        this.loanSettingService = loanSettingService;
        this.specialLimitService = specialLimitService;
        this.userService = userService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;

        // Initialize the layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Catalogue des documents");
        add(title);

        // Get current user information
        initializeCurrentUser();

        // Create search layout
        createSearchLayout(categoryService, publisherService);

        // Create results grid
        createResultsGrid();

        // Create pagination controls
        createPaginationControls();

        // Initial search with empty criteria
        searchItems();
    }

    private void initializeCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<UserDto> user = userService.findByUsername(username);
        if (user.isPresent()) {
            currentUser = user.get();

            // Get reservation limits
            updateReservationLimits();

            // Get active reservations count
            List<ReservationDto> activeReservations = reservationService.findByMember(currentUser.getId()).stream()
                    .filter(r -> "reserved".equals(r.getStatus()) || "ready".equals(r.getStatus()))
                    .collect(Collectors.toList());
            activeReservationCount = activeReservations.size();
        } else {
            Notification.show("Erreur: Impossible de récupérer les informations utilisateur",
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateReservationLimits() {
        // Get default limit from settings
        Optional<LoanSettingDto> settings = loanSettingService.findById(1L);
        if (settings.isPresent()) {
            reservationLimit = currentUser.getIsChild() ? settings.get().getMaxReservationsChild()
                    : settings.get().getMaxReservationsAdult();
        } else {
            // Default fallback values if settings not found
            reservationLimit = currentUser.getIsChild() ? 3 : 5;
        }

        // Check for special limits
        try {
            User user = new User();
            user.setId(currentUser.getId());

            Optional<SpecialLimit> specialLimit = specialLimitService.findFirstByUserOrderByCreatedAtDesc(user);
            if (specialLimit.isPresent() && "active".equals(specialLimit.get().getStatus())) {
                reservationLimit = specialLimit.get().getMaxReservations();
            }
        } catch (Exception e) {
            System.err.println("Error getting special limits: " + e.getMessage());
        }
    }

    private void createSearchLayout(CategoryServiceV2 categoryService, PublisherServiceV2 publisherService) {
        searchLayout = new VerticalLayout();
        searchLayout.setWidth("100%");
        searchLayout.setPadding(true);
        searchLayout.setSpacing(true);

        // Type selection dropdown
        typeComboBox = new ComboBox<>("Type de document");
        typeComboBox.setItems("Tous", "Livre", "Revue", "Jeu");
        typeComboBox.setValue("Tous");
        typeComboBox.addValueChangeListener(e -> {
            selectedType = e.getValue();
            updateSearchFields();
        });

        // Search fields layout
        searchFieldsLayout = new FormLayout();
        searchFieldsLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3));

        // Initialize search fields
        createGeneralSearchFields(categoryService, publisherService);

        // Button layout
        HorizontalLayout buttonLayout = new HorizontalLayout();

        searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> {
            currentPage = 0;
            searchItems();
        });

        resetButton = new Button("Réinitialiser");
        resetButton.addClickListener(e -> {
            resetSearchFields();
            currentPage = 0;
            searchItems();
        });

        buttonLayout.add(searchButton, resetButton);

        searchLayout.add(typeComboBox, searchFieldsLayout, buttonLayout);
        add(searchLayout);
    }

    private void createGeneralSearchFields(CategoryServiceV2 categoryService, PublisherServiceV2 publisherService) {
        // Keyword search
        keywordField = new TextField("Mot-clé");
        keywordField.setPlaceholder("Rechercher dans le titre...");
        keywordField.setValueChangeMode(ValueChangeMode.EAGER);
        keywordField.addValueChangeListener(e -> {
            if (e.getValue().length() >= 3 || e.getValue().isEmpty()) {
                currentPage = 0;
                searchItems();
            }
        });

        // Category combobox
        categoryComboBox = new ComboBox<>("Catégorie");
        categoryComboBox.setItems(categoryService.findAll());
        categoryComboBox.setItemLabelGenerator(CategoryDto::getName);

        // Publisher combobox
        publisherComboBox = new ComboBox<>("Éditeur");
        publisherComboBox.setItems(publisherService.findAll());
        publisherComboBox.setItemLabelGenerator(PublisherDto::getName);

        searchFieldsLayout.removeAll();
        searchFieldsLayout.add(keywordField, categoryComboBox, publisherComboBox);

        // Additional fields as attributes (for other document types)
        titleField = new TextField("Titre");
        authorField = new TextField("Auteur");
        isbnField = new TextField("ISBN");
        publicationDateField = new DatePicker("Date de publication");
        isniField = new TextField("ISNI");
        monthComboBox = new ComboBox<>("Mois");
        monthComboBox.setItems(MoisOption.getListeMois());
        monthComboBox.setItemLabelGenerator(MoisOption::getNom);
        yearField = new IntegerField("Année");
        gtinField = new TextField("GTIN");
        numberOfPiecesField = new IntegerField("Nombre de pièces");
        recommendedAgeField = new IntegerField("Âge recommandé");
    }

    private void updateSearchFields() {
        searchFieldsLayout.removeAll();

        switch (selectedType) {
            case "Livre":
                createBookSearchFields();
                break;
            case "Revue":
                createMagazineSearchFields();
                break;
            case "Jeu":
                createGameSearchFields();
                break;
            default:
                createGeneralSearchFields(null, null); // Just reuse existing comboboxes
                break;
        }
    }

    private void createBookSearchFields() {
        searchFieldsLayout.add(
                titleField,
                authorField,
                isbnField,
                publicationDateField,
                categoryComboBox,
                publisherComboBox);
    }

    private void createMagazineSearchFields() {
        searchFieldsLayout.add(
                titleField,
                isniField,
                monthComboBox,
                yearField,
                categoryComboBox,
                publisherComboBox);
    }

    private void createGameSearchFields() {
        searchFieldsLayout.add(
                titleField,
                gtinField,
                numberOfPiecesField,
                recommendedAgeField,
                categoryComboBox,
                publisherComboBox);
    }

    private void resetSearchFields() {
        // Clear all search fields
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        publicationDateField.clear();
        categoryComboBox.clear();
        publisherComboBox.clear();
        keywordField.clear();
        isniField.clear();
        monthComboBox.clear();
        yearField.clear();
        gtinField.clear();
        numberOfPiecesField.clear();
        recommendedAgeField.clear();

        // Reset type selection
        typeComboBox.setValue("Tous");
        selectedType = "Tous";
        updateSearchFields();
    }

    private void createResultsGrid() {
        resultsGrid = new Grid<>();
        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        resultsGrid.setHeight("500px");

        // Configure columns
        resultsGrid.addColumn(ItemDto::getTitle).setHeader("Titre").setAutoWidth(true).setFlexGrow(1);
        resultsGrid.addColumn(item -> {
            String type = item.getType();
            return switch (type) {
                case "book" -> "Livre";
                case "magazine" -> "Revue";
                case "board_game" -> "Jeu";
                default -> type;
            };
        }).setHeader("Type").setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie").setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur").setAutoWidth(true);

        // Add availability column with icons
        resultsGrid.addComponentColumn(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());

            if (copies.isEmpty()) {
                return new Span("Aucun exemplaire");
            }

            long availableCount = copies.stream()
                    .filter(copy -> "available".equals(copy.getStatus()))
                    .count();

            HorizontalLayout availabilityLayout = new HorizontalLayout();

            if (availableCount > 0) {
                Icon icon = VaadinIcon.CHECK_CIRCLE.create();
                icon.setColor("green");
                availabilityLayout.add(icon, new Span(availableCount + " disponible(s)"));
            } else {
                Icon icon = VaadinIcon.CLOSE_CIRCLE.create();
                icon.setColor("red");
                availabilityLayout.add(icon, new Span("Non disponible"));
            }

            return availabilityLayout;
        }).setHeader("Disponibilité").setAutoWidth(true);

        // Add item click listener
        resultsGrid.addItemClickListener(e -> openItemDetails(e.getItem()));

        // Add grid to layout
        add(resultsGrid);
    }

    private void createPaginationControls() {
        paginationLayout = new HorizontalLayout();
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        prevButton = new Button("Précédent");
        prevButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                searchItems();
            }
        });

        nextButton = new Button("Suivant");
        nextButton.addClickListener(e -> {
            if ((currentPage + 1) * PAGE_SIZE < totalItems) {
                currentPage++;
                searchItems();
            }
        });

        pageInfoLabel = new Span();

        paginationLayout.add(prevButton, pageInfoLabel, nextButton);
        add(paginationLayout);
    }

    private void searchItems() {
        searchCriteria.clear();

        // Get values from appropriate fields based on selected type
        switch (selectedType) {
            case "Livre":
                searchCriteria.put("title", titleField.getValue());
                searchCriteria.put("author", authorField.getValue());
                searchCriteria.put("isbn", isbnField.getValue());
                searchCriteria.put("publicationDate", publicationDateField.getValue());
                searchCriteria.put("category", categoryComboBox.getValue());
                searchCriteria.put("publisher", publisherComboBox.getValue());
                break;
            case "Revue":
                searchCriteria.put("title", titleField.getValue());
                searchCriteria.put("isni", isniField.getValue());
                searchCriteria.put("month",
                        monthComboBox.getValue() != null ? monthComboBox.getValue().getNumero() : "");
                searchCriteria.put("year", yearField.getValue());
                searchCriteria.put("publicationDate", publicationDateField.getValue());
                searchCriteria.put("category", categoryComboBox.getValue());
                searchCriteria.put("publisher", publisherComboBox.getValue());
                break;
            case "Jeu":
                searchCriteria.put("title", titleField.getValue());
                searchCriteria.put("gtin", gtinField.getValue());
                searchCriteria.put("numberOfPieces", numberOfPiecesField.getValue());
                searchCriteria.put("recommendedAge", recommendedAgeField.getValue());
                searchCriteria.put("category", categoryComboBox.getValue());
                searchCriteria.put("publisher", publisherComboBox.getValue());
                break;
            default:
                searchCriteria.put("keyword", keywordField.getValue());
                searchCriteria.put("category", categoryComboBox.getValue());
                searchCriteria.put("publisher", publisherComboBox.getValue());
                break;
        }

        // Get matching items with pagination
        List<ItemDto> items = itemService.fetchItemsWithFilters(
                searchCriteria,
                selectedType,
                currentPage * PAGE_SIZE,
                PAGE_SIZE);

        // Update grid
        resultsGrid.setItems(items);

        // Get total count for pagination
        totalItems = countTotalItems();

        // Update pagination info
        updatePaginationControls();
    }

    private int countTotalItems() {
        // This is a simplified approach - in a real application, you'd have a dedicated
        // count method
        // to avoid fetching all records
        int total = itemService.fetchItemsWithFilters(searchCriteria, selectedType, 0, Integer.MAX_VALUE).size();
        return total;
    }

    private void updatePaginationControls() {
        int startItem = Math.min(currentPage * PAGE_SIZE + 1, totalItems);
        int endItem = Math.min((currentPage + 1) * PAGE_SIZE, totalItems);

        pageInfoLabel.setText(String.format("Affichage %d - %d sur %d",
                startItem > 0 ? startItem : 0,
                endItem,
                totalItems));

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled((currentPage + 1) * PAGE_SIZE < totalItems);
    }

    private void openItemDetails(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("auto");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        // Item title
        H3 title = new H3(item.getTitle());
        if (item.getLink() != null && !item.getLink().isEmpty()) {
            title.getElement().setProperty("innerHTML",
                    "<a href='" + item.getLink() + "' target='_blank'>" + item.getTitle() + "</a>");
        }

        // Item type
        String typeLabel = "";
        switch (item.getType()) {
            case "book":
                typeLabel = "Livre";
                break;
            case "magazine":
                typeLabel = "Revue";
                break;
            case "board_game":
                typeLabel = "Jeu";
                break;
            default:
                typeLabel = item.getType();
        }

        Div typeDiv = new Div();
        typeDiv.add(new Span("Type: " + typeLabel));

        // Category
        Div categoryDiv = new Div();
        categoryDiv.add(new Span("Catégorie: " + item.getCategory().getName()));

        // Publisher
        Div publisherDiv = new Div();
        publisherDiv.add(new Span("Éditeur: " + item.getPublisher().getName()));

        // Specific details based on type
        VerticalLayout specificDetailsLayout = new VerticalLayout();
        specificDetailsLayout.setPadding(false);
        specificDetailsLayout.setSpacing(false);

        if ("book".equals(item.getType())) {
            // Book-specific details
            addBookDetails(specificDetailsLayout, item.getId());
        } else if ("magazine".equals(item.getType())) {
            // Magazine-specific details
            addMagazineDetails(specificDetailsLayout, item.getId());
        } else if ("board_game".equals(item.getType())) {
            // Board game-specific details
            addBoardGameDetails(specificDetailsLayout, item.getId());
        }

        // Availability section
        H4 availabilityHeader = new H4("Disponibilité");

        List<CopyDto> copies = copyService.findByItem(item.getId());
        VerticalLayout availabilityLayout = new VerticalLayout();
        availabilityLayout.setPadding(false);
        availabilityLayout.setSpacing(false);

        if (copies.isEmpty()) {
            availabilityLayout.add(new Paragraph("Aucun exemplaire disponible"));
        } else {
            long availableCount = copies.stream()
                    .filter(copy -> "available".equals(copy.getStatus()))
                    .count();

            if (availableCount > 0) {
                Paragraph availableParagraph = new Paragraph(
                        availableCount + " exemplaire(s) disponible(s) sur " + copies.size());
                availableParagraph.getStyle().set("color", "green");
                availableParagraph.getStyle().set("font-weight", "bold");
                availabilityLayout.add(availableParagraph);
            } else {
                Paragraph notAvailableParagraph = new Paragraph(
                        "Aucun exemplaire disponible actuellement sur " + copies.size());
                notAvailableParagraph.getStyle().set("color", "red");
                availabilityLayout.add(notAvailableParagraph);

                // Check current reservations
                List<ReservationDto> itemReservations = copies.stream()
                        .flatMap(copy -> reservationService.findByCopy(copy.getId()).stream())
                        .filter(res -> "reserved".equals(res.getStatus()) || "ready".equals(res.getStatus()))
                        .collect(Collectors.toList());

                if (!itemReservations.isEmpty()) {
                    availabilityLayout.add(new Paragraph(
                            "Ce document a actuellement " + itemReservations.size() + " réservation(s)"));
                }
            }
        }

        // Reservation button (if applicable)
        HorizontalLayout actionsLayout = new HorizontalLayout();

        boolean canReserve = false;
        CopyDto copyToReserve = null;

        // Check if there's at least one copy (even if not available)
        if (!copies.isEmpty()) {
            // Check if user has reached reservation limit
            if (activeReservationCount >= reservationLimit) {
                Paragraph limitReachedText = new Paragraph(
                        "Vous avez atteint votre limite de réservations (" +
                                activeReservationCount + "/" + reservationLimit + ")");
                limitReachedText.getStyle().set("color", "red");
                actionsLayout.add(limitReachedText);
            } else {
                // Check if user already has a reservation for this item
                boolean hasExistingReservation = copies.stream()
                        .flatMap(copy -> reservationService.findByCopy(copy.getId()).stream())
                        .anyMatch(res -> res.getMember().getId().equals(currentUser.getId()) &&
                                ("reserved".equals(res.getStatus()) || "ready".equals(res.getStatus())));

                if (hasExistingReservation) {
                    Paragraph alreadyReservedText = new Paragraph("Vous avez déjà réservé ce document");
                    alreadyReservedText.getStyle().set("color", "blue");
                    actionsLayout.add(alreadyReservedText);
                } else {
                    // Find a copy for reservation (can be any copy, even if borrowed)
                    copyToReserve = copies.stream()
                            .filter(copy -> !"deleted".equals(copy.getStatus()))
                            .findFirst()
                            .orElse(null);

                    canReserve = (copyToReserve != null);
                }
            }
        }

        // Add reservation button if applicable
        if (canReserve) {
            CopyDto finalCopyToReserve = copyToReserve;
            Button reserveButton = new Button("Réserver");
            reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            // Reservation button action
            reserveButton.addClickListener(e -> {
                try {
                    // Create new reservation
                    ReservationDto newReservation = new ReservationDto();
                    newReservation.setCopy(finalCopyToReserve);
                    newReservation.setMember(currentUser);
                    newReservation.setReservationDate(LocalDate.now());
                    newReservation.setStatus("reserved");

                    // Save reservation
                    reservationService.save(newReservation);

                    // Update UI
                    Notification.show("Document réservé avec succès",
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    // Update active reservation count
                    activeReservationCount++;

                    // Close dialog
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Erreur lors de la réservation: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            actionsLayout.add(reserveButton);
        }

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.addClickListener(e -> dialog.close());
        actionsLayout.add(closeButton);

        // Add all components to dialog
        dialogLayout.add(
                title,
                typeDiv,
                categoryDiv,
                publisherDiv,
                specificDetailsLayout,
                availabilityHeader,
                availabilityLayout,
                actionsLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void addBookDetails(VerticalLayout layout, Long itemId) {
        Optional<BookDto> book = bookService.findById(itemId);
        if (book.isPresent()) {
            BookDto bookData = book.get();

            // Book author
            Div authorDiv = new Div();
            authorDiv.add(new Span("Auteur: " + bookData.getAuthor()));

            // ISBN
            Div isbnDiv = new Div();
            isbnDiv.add(new Span("ISBN: " + bookData.getIsbn()));

            // Publication date
            Div pubDateDiv = new Div();
            if (bookData.getPublicationDate() != null) {
                pubDateDiv.add(new Span("Date de publication: " +
                        bookData.getPublicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }

            layout.add(authorDiv, isbnDiv, pubDateDiv);
        }
    }

    private void addMagazineDetails(VerticalLayout layout, Long itemId) {
        Optional<MagazineDto> magazine = magazineService.findById(itemId);
        if (magazine.isPresent()) {
            MagazineDto magData = magazine.get();

            // ISNI
            Div isniDiv = new Div();
            isniDiv.add(new Span("ISNI: " + magData.getIsni()));

            // Month/Year
            Div periodDiv = new Div();
            periodDiv.add(new Span("Période: " + magData.getMonth() + " " + magData.getYear()));

            // Publication date
            Div pubDateDiv = new Div();
            if (magData.getPublicationDate() != null) {
                pubDateDiv.add(new Span("Date de publication: " +
                        magData.getPublicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }

            layout.add(isniDiv, periodDiv, pubDateDiv);
        }
    }

    private void addBoardGameDetails(VerticalLayout layout, Long itemId) {
        Optional<BoardGameDto> game = boardGameService.findById(itemId);
        if (game.isPresent()) {
            BoardGameDto gameData = game.get();

            // Number of pieces
            Div piecesDiv = new Div();
            piecesDiv.add(new Span("Nombre de pièces: " + gameData.getNumberOfPieces()));

            // Recommended age
            Div ageDiv = new Div();
            ageDiv.add(new Span("Âge recommandé: " + gameData.getRecommendedAge() + " ans et plus"));

            // GTIN
            Div gtinDiv = new Div();
            gtinDiv.add(new Span("GTIN: " + gameData.getGtin()));

            // Game rules (truncated)
            Div rulesDiv = new Div();
            String rules = gameData.getGameRules();
            if (rules != null && !rules.isEmpty()) {
                String truncatedRules = rules.length() > 200 ? rules.substring(0, 200) + "..." : rules;
                rulesDiv.add(new H5("Règles du jeu:"));
                rulesDiv.add(new Paragraph(truncatedRules));
            }

            layout.add(piecesDiv, ageDiv, gtinDiv, rulesDiv);
        }
    }
}