package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.*;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Gestion du Catalogue")
@Route(value = "volunteer/catalog-management", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleCatalogueView extends VerticalLayout {

    // Services
    private final ItemServiceV2 itemService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final CategoryServiceV2 categoryService;
    private final PublisherServiceV2 publisherService;
    private final CopyServiceV2 copyService;
    private final SupplierServiceV2 supplierService;

    // UI Components
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<CategoryDto> categoryFilter;
    private Grid<ItemDto> resultsGrid;
    private Button searchButton;
    private Button resetButton;
    private Button addDocumentButton;

    public BenevoleCatalogueView(ItemServiceV2 itemService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CategoryServiceV2 categoryService,
            PublisherServiceV2 publisherService,
            CopyServiceV2 copyService,
            SupplierServiceV2 supplierService) {
        this.itemService = itemService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.categoryService = categoryService;
        this.publisherService = publisherService;
        this.copyService = copyService;
        this.supplierService = supplierService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Create components
        createHeader();
        createSearchBar();
        createResultsGrid();

        // Initial search with empty criteria
        searchItems();
    }

    private void createHeader() {
        H2 title = new H2("Gestion du Catalogue");

        addDocumentButton = new Button("Ajouter un document", new Icon(VaadinIcon.PLUS));
        addDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addDocumentButton.addClickListener(e -> navigateToAddDocument());

        HorizontalLayout headerLayout = new HorizontalLayout(title, addDocumentButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        add(headerLayout);
    }

    private void navigateToAddDocument() {
        // Redirect to the BenevoleAjouterView
        getUI().ifPresent(ui -> ui.navigate("volunteer/add"));
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

        // Copies info column
        resultsGrid.addColumn(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());
            return copies.size() + " exemplaire(s)";
        }).setHeader("Exemplaires").setAutoWidth(true);

        // Available copies column
        resultsGrid.addColumn(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());
            long availableCount = copies.stream().filter(c -> "available".equals(c.getStatus())).count();
            return availableCount + " disponible(s)";
        }).setHeader("Disponibles").setAutoWidth(true);

        // Action buttons column
        resultsGrid.addComponentColumn(item -> {
            HorizontalLayout actionButtons = new HorizontalLayout();

            Button editButton = new Button("Éditer");
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> editItemDetails(item));

            Button copyButton = new Button("Exemplaires");
            copyButton.addClickListener(e -> manageCopies(item));

            Button deleteButton = new Button("Supprimer");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteItem(item));

            actionButtons.add(editButton, copyButton, deleteButton);
            actionButtons.setSpacing(true);

            return actionButtons;
        }).setHeader("Actions").setAutoWidth(true);

        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        resultsGrid.setHeight("600px");

        // Double-click to edit
        resultsGrid.addItemDoubleClickListener(event -> editItemDetails(event.getItem()));

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

    private void editItemDetails(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");
        dialog.setHeight("700px");
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Modifier le document");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setSizeFull();

        // Create tabs for different sections
        Tab basicInfoTab = new Tab("Informations générales");
        Tab specificInfoTab = new Tab("Informations spécifiques");

        Tabs tabs = new Tabs(basicInfoTab, specificInfoTab);
        tabs.setWidthFull();

        // Tab content containers
        VerticalLayout basicInfoLayout = createBasicInfoForm(item);
        VerticalLayout specificInfoLayout = createSpecificInfoForm(item);

        // Initially show basic info
        specificInfoLayout.setVisible(false);

        // Tab change listener
        tabs.addSelectedChangeListener(event -> {
            basicInfoLayout.setVisible(event.getSelectedTab().equals(basicInfoTab));
            specificInfoLayout.setVisible(event.getSelectedTab().equals(specificInfoTab));
        });

        // Bottom buttons
        Button saveButton = new Button("Enregistrer");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            saveItemChanges(item, basicInfoLayout, specificInfoLayout, dialog);
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(tabs, basicInfoLayout, specificInfoLayout, buttonLayout);
        dialogLayout.setFlexGrow(1, basicInfoLayout);
        dialogLayout.setFlexGrow(1, specificInfoLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private VerticalLayout createBasicInfoForm(ItemDto item) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Title field
        TextField titleField = new TextField("Titre");
        titleField.setValue(item.getTitle());
        titleField.setWidthFull();
        titleField.setRequired(true);
        titleField.setId("title-field");

        // Type field (readonly as it can't be changed)
        String typeLabel = switch (item.getType()) {
            case "book" -> "Livre";
            case "magazine" -> "Revue";
            case "board_game" -> "Jeu";
            default -> item.getType();
        };
        TextField typeField = new TextField("Type");
        typeField.setValue(typeLabel);
        typeField.setReadOnly(true);

        // Category selector
        ComboBox<CategoryDto> categoryField = new ComboBox<>("Catégorie");
        categoryField.setItems(categoryService.findAll());
        categoryField.setItemLabelGenerator(CategoryDto::getName);
        categoryField.setValue(item.getCategory());
        categoryField.setRequired(true);
        categoryField.setId("category-field");

        // Publisher selector
        ComboBox<PublisherDto> publisherField = new ComboBox<>("Éditeur");
        publisherField.setItems(publisherService.findAll());
        publisherField.setItemLabelGenerator(PublisherDto::getName);
        publisherField.setValue(item.getPublisher());
        publisherField.setRequired(true);
        publisherField.setId("publisher-field");

        // Value field
        TextField valueField = new TextField("Valeur");
        valueField.setValue(item.getValue().toString());
        valueField.setId("value-field");

        // Link field
        TextField linkField = new TextField("Lien externe");
        if (item.getLink() != null) {
            linkField.setValue(item.getLink());
        }
        linkField.setId("link-field");

        // Add fields to form
        formLayout.add(titleField, typeField, categoryField, publisherField, valueField, linkField);

        layout.add(formLayout);
        return layout;
    }

    private VerticalLayout createSpecificInfoForm(ItemDto item) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        String itemType = item.getType();

        if ("book".equals(itemType)) {
            Optional<BookDto> book = bookService.findById(item.getId());
            if (book.isPresent()) {
                BookDto bookData = book.get();

                TextField authorField = new TextField("Auteur");
                authorField.setValue(bookData.getAuthor());
                authorField.setRequired(true);
                authorField.setId("author-field");

                TextField isbnField = new TextField("ISBN");
                isbnField.setValue(bookData.getIsbn());
                isbnField.setReadOnly(true); // ISBN is a unique identifier, shouldn't be changed

                DatePicker pubDateField = new DatePicker("Date de publication");
                pubDateField.setValue(bookData.getPublicationDate());
                pubDateField.setRequired(true);
                pubDateField.setId("pub-date-field");

                formLayout.add(authorField, isbnField, pubDateField);
            }
        } else if ("magazine".equals(itemType)) {
            Optional<MagazineDto> magazine = magazineService.findById(item.getId());
            if (magazine.isPresent()) {
                MagazineDto magazineData = magazine.get();

                TextField isniField = new TextField("ISNI");
                isniField.setValue(magazineData.getIsni());
                isniField.setReadOnly(true); // ISNI is a unique identifier

                ComboBox<MoisOption> monthField = new ComboBox<>("Mois");
                monthField.setItems(MoisOption.getListeMois());
                monthField.setItemLabelGenerator(MoisOption::getNom);

                // Find the month option
                Optional<MoisOption> selectedMonth = MoisOption.getListeMois().stream()
                        .filter(m -> m.getNom().equals(magazineData.getMonth()))
                        .findFirst();
                selectedMonth.ifPresent(monthField::setValue);
                monthField.setReadOnly(true); // Month is part of unique identifier

                TextField yearField = new TextField("Année");
                yearField.setValue(magazineData.getYear());
                yearField.setReadOnly(true); // Year is part of unique identifier

                DatePicker pubDateField = new DatePicker("Date de publication");
                pubDateField.setValue(magazineData.getPublicationDate());
                pubDateField.setRequired(true);
                pubDateField.setId("pub-date-field");

                formLayout.add(isniField, monthField, yearField, pubDateField);
            }
        } else if ("board_game".equals(itemType)) {
            Optional<BoardGameDto> boardGame = boardGameService.findById(item.getId());
            if (boardGame.isPresent()) {
                BoardGameDto gameData = boardGame.get();

                IntegerField piecesField = new IntegerField("Nombre de pièces");
                piecesField.setValue(gameData.getNumberOfPieces());
                piecesField.setRequired(true);
                piecesField.setId("pieces-field");

                IntegerField ageField = new IntegerField("Âge recommandé");
                ageField.setValue(gameData.getRecommendedAge());
                ageField.setRequired(true);
                ageField.setId("age-field");

                TextField gtinField = new TextField("GTIN");
                gtinField.setValue(gameData.getGtin());
                gtinField.setReadOnly(true); // GTIN is a unique identifier

                TextArea rulesField = new TextArea("Règles du jeu");
                rulesField.setValue(gameData.getGameRules());
                rulesField.setMinHeight("200px");
                rulesField.setId("rules-field");

                formLayout.add(piecesField, ageField, gtinField);

                // Add rules as a full-width component
                layout.add(formLayout, new H4("Règles du jeu"), rulesField);
                return layout;
            }
        }

        layout.add(formLayout);
        return layout;
    }

    private void saveItemChanges(ItemDto item, VerticalLayout basicInfoLayout,
            VerticalLayout specificInfoLayout, Dialog dialog) {
        try {
            // Update general item information
            TextField titleField = (TextField) basicInfoLayout.getChildren()
                    .filter(component -> component instanceof FormLayout)
                    .findFirst()
                    .orElseThrow()
                    .getChildren()
                    .filter(component -> component instanceof TextField)
                    .filter(component -> "title-field".equals(component.getId().orElse("")))
                    .findFirst()
                    .orElseThrow();

            ComboBox<CategoryDto> categoryField = (ComboBox<CategoryDto>) basicInfoLayout.getChildren()
                    .filter(component -> component instanceof FormLayout)
                    .findFirst()
                    .orElseThrow()
                    .getChildren()
                    .filter(component -> component instanceof ComboBox<?>)
                    .filter(component -> "category-field".equals(component.getId().orElse("")))
                    .findFirst()
                    .orElseThrow();

            ComboBox<PublisherDto> publisherField = (ComboBox<PublisherDto>) basicInfoLayout.getChildren()
                    .filter(component -> component instanceof FormLayout)
                    .findFirst()
                    .orElseThrow()
                    .getChildren()
                    .filter(component -> component instanceof ComboBox<?>)
                    .filter(component -> "publisher-field".equals(component.getId().orElse("")))
                    .findFirst()
                    .orElseThrow();

            TextField valueField = (TextField) basicInfoLayout.getChildren()
                    .filter(component -> component instanceof FormLayout)
                    .findFirst()
                    .orElseThrow()
                    .getChildren()
                    .filter(component -> component instanceof TextField)
                    .filter(component -> "value-field".equals(component.getId().orElse("")))
                    .findFirst()
                    .orElseThrow();

            TextField linkField = (TextField) basicInfoLayout.getChildren()
                    .filter(component -> component instanceof FormLayout)
                    .findFirst()
                    .orElseThrow()
                    .getChildren()
                    .filter(component -> component instanceof TextField)
                    .filter(component -> "link-field".equals(component.getId().orElse("")))
                    .findFirst()
                    .orElseThrow();

            // Update basic item data
            item.setTitle(titleField.getValue());
            item.setCategory(categoryField.getValue());
            item.setPublisher(publisherField.getValue());
            item.setValue(Double.parseDouble(valueField.getValue()));
            item.setLink(linkField.getValue());

            // Update type-specific data
            String itemType = item.getType();
            if ("book".equals(itemType)) {
                Optional<BookDto> bookOptional = bookService.findById(item.getId());
                if (bookOptional.isPresent()) {
                    BookDto book = bookOptional.get();

                    TextField authorField = (TextField) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof FormLayout)
                            .findFirst()
                            .orElseThrow()
                            .getChildren()
                            .filter(component -> component instanceof TextField)
                            .filter(component -> "author-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    DatePicker pubDateField = (DatePicker) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof FormLayout)
                            .findFirst()
                            .orElseThrow()
                            .getChildren()
                            .filter(component -> component instanceof DatePicker)
                            .filter(component -> "pub-date-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    book.setAuthor(authorField.getValue());
                    book.setPublicationDate(pubDateField.getValue());
                    book.setItem(item);

                    bookService.save(book);
                }
            } else if ("magazine".equals(itemType)) {
                Optional<MagazineDto> magazineOptional = magazineService.findById(item.getId());
                if (magazineOptional.isPresent()) {
                    MagazineDto magazine = magazineOptional.get();

                    DatePicker pubDateField = (DatePicker) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof FormLayout)
                            .findFirst()
                            .orElseThrow()
                            .getChildren()
                            .filter(component -> component instanceof DatePicker)
                            .filter(component -> "pub-date-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    magazine.setPublicationDate(pubDateField.getValue());
                    magazine.setItem(item);

                    magazineService.save(magazine);
                }
            } else if ("board_game".equals(itemType)) {
                Optional<BoardGameDto> gameOptional = boardGameService.findById(item.getId());
                if (gameOptional.isPresent()) {
                    BoardGameDto game = gameOptional.get();

                    IntegerField piecesField = (IntegerField) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof FormLayout)
                            .findFirst()
                            .orElseThrow()
                            .getChildren()
                            .filter(component -> component instanceof IntegerField)
                            .filter(component -> "pieces-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    IntegerField ageField = (IntegerField) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof FormLayout)
                            .findFirst()
                            .orElseThrow()
                            .getChildren()
                            .filter(component -> component instanceof IntegerField)
                            .filter(component -> "age-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    TextArea rulesField = (TextArea) specificInfoLayout.getChildren()
                            .filter(component -> component instanceof TextArea)
                            .filter(component -> "rules-field".equals(component.getId().orElse("")))
                            .findFirst()
                            .orElseThrow();

                    game.setNumberOfPieces(piecesField.getValue());
                    game.setRecommendedAge(ageField.getValue());
                    game.setGameRules(rulesField.getValue());
                    game.setItem(item);

                    boardGameService.save(game, true);
                }
            }

            // Save the item itself
            itemService.save(item);

            // Show success message
            Notification.show("Document mis à jour avec succès", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Close dialog and refresh grid
            dialog.close();
            searchItems();

        } catch (Exception e) {
            Notification.show("Erreur lors de la mise à jour: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void manageCopies(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setHeaderTitle("Gestion des exemplaires - " + item.getTitle());

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setSizeFull();

        // Get copies of this item
        List<CopyDto> copies = copyService.findByItem(item.getId());

        // Summary info
        H4 summaryTitle = new H4("Exemplaires");
        long totalCopies = copies.size();
        long availableCopies = copies.stream().filter(copy -> "available".equals(copy.getStatus())).count();
        long borrowedCopies = copies.stream().filter(copy -> "borrowed".equals(copy.getStatus())).count();
        long reservedCopies = copies.stream().filter(copy -> "reserved".equals(copy.getStatus())).count();
        long unavailableCopies = copies.stream().filter(copy -> "unavailable".equals(copy.getStatus())).count();

        Paragraph summary = new Paragraph(
                String.format("Total: %d | Disponibles: %d | Empruntés: %d | Réservés: %d | Indisponibles: %d",
                        totalCopies, availableCopies, borrowedCopies, reservedCopies, unavailableCopies));

        // Copies grid
        Grid<CopyDto> copiesGrid = new Grid<>();
        copiesGrid.setItems(copies);

        copiesGrid.addColumn(CopyDto::getId).setHeader("ID").setSortable(true).setWidth("80px").setFlexGrow(0);

        copiesGrid.addColumn(copy -> {
            String status = copy.getStatus();
            return switch (status) {
                case "available" -> "Disponible";
                case "borrowed" -> "Emprunté";
                case "reserved" -> "Réservé";
                case "unavailable" -> "Indisponible";
                default -> status;
            };
        }).setHeader("Statut").setSortable(true).setWidth("120px").setFlexGrow(0);

        copiesGrid.addColumn(copy -> {
            if (copy.getAcquisitionDate() != null) {
                return copy.getAcquisitionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return "";
        }).setHeader("Date d'acquisition").setSortable(true).setWidth("150px").setFlexGrow(0);

        copiesGrid.addColumn(CopyDto::getPrice).setHeader("Prix").setSortable(true).setWidth("100px").setFlexGrow(0);

        // Action buttons
        copiesGrid.addComponentColumn(copy -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button toggleStatusButton;
            if ("available".equals(copy.getStatus())) {
                toggleStatusButton = new Button("Marquer indisponible");
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                toggleStatusButton.addClickListener(e -> updateCopyStatus(copy, "unavailable", copiesGrid));
            } else if ("unavailable".equals(copy.getStatus())) {
                toggleStatusButton = new Button("Marquer disponible");
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                toggleStatusButton.addClickListener(e -> updateCopyStatus(copy, "available", copiesGrid));
            } else {
                // For borrowed or reserved, we show an inactive button
                toggleStatusButton = new Button(
                        "borrowed".equals(copy.getStatus()) ? "Emprunté" : "Réservé");
                toggleStatusButton.setEnabled(false);
            }

            actions.add(toggleStatusButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        copiesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        copiesGrid.setHeight("350px");

        // Add new copy button
        Button addCopyButton = new Button("Ajouter un exemplaire");
        addCopyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCopyButton.addClickListener(e -> showAddCopyDialog(item, copiesGrid));

        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(addCopyButton, closeButton);
        buttonLayout.setSpacing(true);

        dialogLayout.add(summaryTitle, summary, copiesGrid, buttonLayout);
        dialogLayout.setFlexGrow(1, copiesGrid);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void updateCopyStatus(CopyDto copy, String newStatus, Grid<CopyDto> grid) {
        try {
            copy.setStatus(newStatus);
            copyService.save(copy);

            // Refresh the grid
            List<CopyDto> updatedCopies = copyService.findByItem(copy.getItem().getId());
            grid.setItems(updatedCopies);

            Notification.show(
                    "Statut de l'exemplaire mis à jour",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show(
                    "Erreur lors de la mise à jour du statut: " + e.getMessage(),
                    5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showAddCopyDialog(ItemDto item, Grid<CopyDto> grid) {
        Dialog addDialog = new Dialog();
        addDialog.setWidth("500px");
        addDialog.setHeaderTitle("Ajouter un exemplaire");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Date field
        DatePicker acquisitionDate = new DatePicker("Date d'acquisition");
        acquisitionDate.setValue(LocalDate.now());
        acquisitionDate.setRequired(true);

        // Price field
        TextField priceField = new TextField("Prix");
        priceField.setValue("0.0");
        priceField.setRequired(true);

        // Status select
        ComboBox<String> statusField = new ComboBox<>("Statut");
        statusField.setItems("available", "unavailable");
        statusField.setValue("available");
        statusField.setItemLabelGenerator(status -> {
            return switch (status) {
                case "available" -> "Disponible";
                case "unavailable" -> "Indisponible";
                default -> status;
            };
        });

        formLayout.add(acquisitionDate, priceField, statusField);

        // Buttons
        Button saveButton = new Button("Enregistrer");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                // Validate inputs
                if (acquisitionDate.isEmpty() || priceField.isEmpty() || statusField.isEmpty()) {
                    Notification.show("Veuillez remplir tous les champs",
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // Create new copy
                CopyDto newCopy = new CopyDto();
                newCopy.setItem(item);
                newCopy.setAcquisitionDate(acquisitionDate.getValue());
                try {
                    newCopy.setPrice(Double.parseDouble(priceField.getValue().replace(",", ".")));
                } catch (NumberFormatException ex) {
                    Notification.show("Format de prix invalide",
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                newCopy.setStatus(statusField.getValue());

                // Save copy
                copyService.save(newCopy);

                // Update grid
                List<CopyDto> updatedCopies = copyService.findByItem(item.getId());
                grid.setItems(updatedCopies);

                Notification.show("Exemplaire ajouté avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                addDialog.close();

            } catch (Exception ex) {
                Notification.show("Erreur: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> addDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.add(formLayout, buttonLayout);

        addDialog.add(layout);
        addDialog.open();
    }

    private void confirmDeleteItem(ItemDto item) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmer la suppression");
        confirmDialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H4 warningTitle = new H4("Attention");
        warningTitle.getStyle().set("color", "var(--lumo-error-color)");

        Paragraph warning = new Paragraph(
                "Cette action va rendre tous les exemplaires de \"" + item.getTitle() +
                        "\" indisponibles. Le document ne sera pas supprimé de la base de données mais " +
                        "ne sera plus disponible pour les emprunts et les réservations.");

        Checkbox confirmCheck = new Checkbox("Je comprends les conséquences de cette action");

        Button deleteButton = new Button("Supprimer");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setEnabled(false);

        confirmCheck.addValueChangeListener(e -> {
            deleteButton.setEnabled(e.getValue());
        });

        deleteButton.addClickListener(e -> {
            try {
                // Get all copies of this item
                List<CopyDto> copies = copyService.findByItem(item.getId());

                // Update all copies to unavailable
                for (CopyDto copy : copies) {
                    copy.setStatus("unavailable");
                    copyService.save(copy);
                }

                Notification.show(
                        "Document marqué comme supprimé avec succès",
                        3000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                confirmDialog.close();
                searchItems(); // Refresh the grid

            } catch (Exception ex) {
                Notification.show(
                        "Erreur lors de la suppression: " + ex.getMessage(),
                        5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(deleteButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.add(warningTitle, warning, confirmCheck, buttonLayout);

        confirmDialog.add(layout);
        confirmDialog.open();
    }
}