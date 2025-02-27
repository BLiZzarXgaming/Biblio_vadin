package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.MoisOption;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PageTitle("Gestion du Catalogue")
@Route(value = "volunteer/catalogue-management", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleCatalogueView extends VerticalLayout {
    private ComboBox<String> typeComboBox;
    private FormLayout searchFieldsLayout;

    private Button searchButton;
    private Button clearButton;

    private Grid<ItemDto> resultsGrid;

    // Services
    private ItemServiceV2 itemService;
    private PublisherServiceV2 publisherService;
    private CategoryServiceV2 categoryService;
    private SupplierServiceV2 supplierService;
    private BookServiceV2 bookService;
    private MagazineServiceV2 magazineService;
    private BoardGameServiceV2 boardGameService;
    private CopyServiceV2 copyService;

    // Pagination
    private int pageSize = 10;
    private int currentPage = 0;
    private Span paginationInfo;
    private Button prevButton;
    private Button nextButton;

    // Search criteria
    private String selectedType;
    private Map<String, Object> searchCriteria = new HashMap<>();

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

    private IntegerField numberOfPiecesField;
    private IntegerField recommendedAgeField;
    private TextField gtinField;

    public BenevoleCatalogueView(ItemServiceV2 itemService,
            PublisherServiceV2 publisherService,
            CategoryServiceV2 categoryService,
            SupplierServiceV2 supplierService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CopyServiceV2 copyService) {
        this.itemService = itemService;
        this.publisherService = publisherService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.copyService = copyService;

        setWidth("100%");
        getStyle().set("flex-grow", "1");
        setHeight("100%");

        H2 title = new H2("Gestion du Catalogue");
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

        // Add document button
        Button addDocumentButton = new Button("Ajouter un document", e -> {
            // Navigate to the document addition view
            getUI().ifPresent(ui -> ui.navigate("volunteer/add"));
        });
        addDocumentButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout actionBar = new HorizontalLayout(searchButton, clearButton, addDocumentButton);
        actionBar.setSpacing(true);

        add(createSearchLayout(), actionBar);
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
        List<CategoryDto> categories = categoryService.findAll();
        categoryComboBox.setItems(categories);
        categoryComboBox.setItemLabelGenerator(CategoryDto::getName);
    }

    private void updatePublisherComboBox() {
        List<PublisherDto> publishers = publisherService.findAll();
        publisherComboBox.setItems(publishers);
        publisherComboBox.setItemLabelGenerator(PublisherDto::getName);
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

        // Add copies status column
        resultsGrid.addColumn(new ComponentRenderer<>(item -> {
            List<CopyDto> copies = copyService.findByItem(item.getId());

            HorizontalLayout badgesLayout = new HorizontalLayout();
            badgesLayout.setSpacing(true);

            long availableCount = copies.stream()
                    .filter(copy -> "available".equals(copy.getStatus()))
                    .count();

            long totalCount = copies.size();

            if (totalCount == 0) {
                Span noCopies = new Span("Aucun exemplaire");
                noCopies.getElement().getThemeList().add("badge");
                noCopies.getElement().getThemeList().add("error");
                badgesLayout.add(noCopies);
            } else {
                Span availableBadge = new Span(availableCount + " / " + totalCount);
                availableBadge.getElement().getThemeList().add("badge");

                if (availableCount > 0) {
                    availableBadge.getElement().getThemeList().add("success");
                } else {
                    availableBadge.getElement().getThemeList().add("error");
                }

                badgesLayout.add(availableBadge);
            }

            return badgesLayout;
        })).setHeader("Exemplaires").setAutoWidth(true);

        // Action column
        resultsGrid.addComponentColumn(item -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setSpacing(true);

            // Edit button
            Button editButton = new Button(new Icon(VaadinIcon.EDIT), e -> editItemDetails(item));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Modifier");

            // Manage copies button
            Button copiesButton = new Button(new Icon(VaadinIcon.COPY), e -> manageCopies(item));
            copiesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            copiesButton.getElement().setAttribute("title", "Gérer les exemplaires");

            // Delete button
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDeleteItem(item));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setAttribute("title", "Supprimer");

            actionsLayout.add(editButton, copiesButton, deleteButton);
            return actionsLayout;
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

    private void editItemDetails(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        H3 title = new H3("Modifier " + translateType(item.getType()));

        // Basic item information
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Title field
        TextField titleField = new TextField("Titre");
        titleField.setValue(item.getTitle());
        titleField.setWidthFull();

        // Category selection
        ComboBox<CategoryDto> categoryField = new ComboBox<>("Catégorie");
        categoryField.setItems(categoryService.findAll());
        categoryField.setItemLabelGenerator(CategoryDto::getName);
        categoryField.setValue(item.getCategory());

        // Publisher selection
        ComboBox<PublisherDto> publisherField = new ComboBox<>("Éditeur");
        publisherField.setItems(publisherService.findAll());
        publisherField.setItemLabelGenerator(PublisherDto::getName);
        publisherField.setValue(item.getPublisher());

        // Link field
        TextField linkField = new TextField("Lien externe");
        linkField.setValue(item.getLink() != null ? item.getLink() : "");
        linkField.setWidthFull();

        formLayout.add(titleField, categoryField, publisherField, linkField);

        // Type-specific fields
        FormLayout specificFormLayout = new FormLayout();
        specificFormLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        if ("book".equals(item.getType())) {
            Optional<BookDto> book = bookService.findById(item.getId());
            if (book.isPresent()) {
                BookDto bookData = book.get();

                TextField authorField = new TextField("Auteur");
                authorField.setValue(bookData.getAuthor());

                TextField isbnField = new TextField("ISBN");
                isbnField.setValue(bookData.getIsbn());
                isbnField.setReadOnly(true); // ISBN shouldn't be changed

                DatePicker publicationDateField = new DatePicker("Date de publication");
                publicationDateField.setValue(bookData.getPublicationDate());

                specificFormLayout.add(authorField, isbnField, publicationDateField);

                // Save button action
                Button saveButton = new Button("Enregistrer", e -> {
                    // Update item data
                    item.setTitle(titleField.getValue());
                    item.setCategory(categoryField.getValue());
                    item.setPublisher(publisherField.getValue());
                    item.setLink(linkField.getValue());

                    // Update book data
                    bookData.setAuthor(authorField.getValue());
                    bookData.setPublicationDate(publicationDateField.getValue());

                    // Save changes
                    try {
                        itemService.save(item);
                        bookService.save(bookData);

                        Notification.show("Document mis à jour avec succès",
                                3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        dialog.close();
                        updateResults();
                    } catch (Exception ex) {
                        Notification.show("Erreur lors de la mise à jour: " + ex.getMessage(),
                                5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelButton = new Button("Annuler", e -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
                buttonsLayout.setSpacing(true);

                dialogLayout.add(title, formLayout, specificFormLayout, buttonsLayout);
            }
        } else if ("magazine".equals(item.getType())) {
            Optional<MagazineDto> magazine = magazineService.findById(item.getId());
            if (magazine.isPresent()) {
                MagazineDto magazineData = magazine.get();

                TextField isniField = new TextField("ISNI");
                isniField.setValue(magazineData.getIsni());
                isniField.setReadOnly(true); // ISNI shouldn't be changed

                ComboBox<MoisOption> monthField = new ComboBox<>("Mois");
                monthField.setItems(MoisOption.getListeMois());
                monthField.setItemLabelGenerator(MoisOption::getNom);
                monthField.setValue(MoisOption.getListeMois().stream()
                        .filter(m -> m.getNumero().equals(magazineData.getMonth()))
                        .findFirst().orElse(null));
                monthField.setReadOnly(true); // Month shouldn't be changed

                IntegerField yearField = new IntegerField("Année");
                yearField.setValue(Integer.parseInt(magazineData.getYear()));
                yearField.setReadOnly(true); // Year shouldn't be changed

                DatePicker publicationDateField = new DatePicker("Date de publication");
                publicationDateField.setValue(magazineData.getPublicationDate());

                specificFormLayout.add(isniField, monthField, yearField, publicationDateField);

                // Save button action
                Button saveButton = new Button("Enregistrer", e -> {
                    // Update item data
                    item.setTitle(titleField.getValue());
                    item.setCategory(categoryField.getValue());
                    item.setPublisher(publisherField.getValue());
                    item.setLink(linkField.getValue());

                    // Update magazine data
                    magazineData.setPublicationDate(publicationDateField.getValue());

                    // Save changes
                    try {
                        itemService.save(item);
                        magazineService.save(magazineData);

                        Notification.show("Document mis à jour avec succès",
                                3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        dialog.close();
                        updateResults();
                    } catch (Exception ex) {
                        Notification.show("Erreur lors de la mise à jour: " + ex.getMessage(),
                                5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelButton = new Button("Annuler", e -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
                buttonsLayout.setSpacing(true);

                dialogLayout.add(title, formLayout, specificFormLayout, buttonsLayout);
            }
        } else if ("board_game".equals(item.getType())) {
            Optional<BoardGameDto> boardGame = boardGameService.findById(item.getId());
            if (boardGame.isPresent()) {
                BoardGameDto gameData = boardGame.get();

                IntegerField piecesField = new IntegerField("Nombre de pièces");
                piecesField.setValue(gameData.getNumberOfPieces());

                IntegerField ageField = new IntegerField("Âge recommandé");
                ageField.setValue(gameData.getRecommendedAge());

                TextField gtinField = new TextField("GTIN");
                gtinField.setValue(gameData.getGtin());
                gtinField.setReadOnly(true); // GTIN shouldn't be changed

                TextArea rulesField = new TextArea("Règles du jeu");
                rulesField.setValue(gameData.getGameRules());
                rulesField.setHeight("150px");

                specificFormLayout.add(piecesField, ageField, gtinField);

                // Save button action
                Button saveButton = new Button("Enregistrer", e -> {
                    // Update item data
                    item.setTitle(titleField.getValue());
                    item.setCategory(categoryField.getValue());
                    item.setPublisher(publisherField.getValue());
                    item.setLink(linkField.getValue());

                    // Update board game data
                    gameData.setNumberOfPieces(piecesField.getValue());
                    gameData.setRecommendedAge(ageField.getValue());
                    gameData.setGameRules(rulesField.getValue());

                    // Save changes
                    try {
                        itemService.save(item);
                        boardGameService.save(gameData, true); // true means it's an update

                        Notification.show("Document mis à jour avec succès",
                                3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        dialog.close();
                        updateResults();
                    } catch (Exception ex) {
                        Notification.show("Erreur lors de la mise à jour: " + ex.getMessage(),
                                5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelButton = new Button("Annuler", e -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
                buttonsLayout.setSpacing(true);

                dialogLayout.add(title, formLayout, specificFormLayout, rulesField, buttonsLayout);
            }
        }

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void manageCopies(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        dialog.setDraggable(true);
        dialog.setResizable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(true);

        H3 title = new H3("Gestion des exemplaires - " + item.getTitle());

        // List of copies
        Grid<CopyDto> copiesGrid = new Grid<>();
        copiesGrid.setHeightFull();

        copiesGrid.addColumn(CopyDto::getId).setHeader("ID").setAutoWidth(true);
        copiesGrid.addColumn(copy -> {
            return copy.getAcquisitionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }).setHeader("Date d'acquisition").setAutoWidth(true);
        copiesGrid.addColumn(CopyDto::getPrice).setHeader("Prix").setAutoWidth(true);

        // Status column with badge
        copiesGrid.addColumn(new ComponentRenderer<>(copy -> {
            Span statusBadge = new Span(translateStatus(copy.getStatus()));
            statusBadge.getElement().getThemeList().add("badge");

            if ("available".equals(copy.getStatus())) {
                statusBadge.getElement().getThemeList().add("success");
            } else if ("deleted".equals(copy.getStatus())) {
                statusBadge.getElement().getThemeList().add("error");
            } else {
                statusBadge.getElement().getThemeList().add("contrast");
            }

            return statusBadge;
        })).setHeader("État").setAutoWidth(true);

        // Actions column
        copiesGrid.addComponentColumn(copy -> {
            HorizontalLayout actions = new HorizontalLayout();

            // Status toggle button
            Button toggleStatusButton;
            if ("available".equals(copy.getStatus())) {
                toggleStatusButton = new Button("Désactiver", e -> {
                    changeCopyStatus(copy, "unavailable");
                    refreshCopiesGrid(copiesGrid, item);
                });
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            } else if ("unavailable".equals(copy.getStatus()) || "deleted".equals(copy.getStatus())) {
                toggleStatusButton = new Button("Activer", e -> {
                    changeCopyStatus(copy, "available");
                    refreshCopiesGrid(copiesGrid, item);
                });
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            } else {
                // For borrowed or reserved copies, can't change status directly
                toggleStatusButton = new Button("Indisponible");
                toggleStatusButton.setEnabled(false);
                toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            }

            // Delete button
            Button deleteButton = new Button("Supprimer", e -> {
                ConfirmDialog confirm = new ConfirmDialog();
                confirm.setHeader("Confirmation");
                confirm.setText("Êtes-vous sûr de vouloir supprimer définitivement cet exemplaire ?");
                confirm.setCancelable(true);

                confirm.setConfirmText("Supprimer");
                confirm.addConfirmListener(event -> {
                    changeCopyStatus(copy, "deleted");
                    refreshCopiesGrid(copiesGrid, item);
                });

                confirm.setCancelText("Annuler");
                confirm.open();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            // Only allow delete if not borrowed or reserved
            if ("borrowed".equals(copy.getStatus()) || "reserved".equals(copy.getStatus())) {
                deleteButton.setEnabled(false);
            }

            actions.add(toggleStatusButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        // Load copies
        refreshCopiesGrid(copiesGrid, item);

        // Add new copy form
        H4 addCopyTitle = new H4("Ajouter un exemplaire");

        FormLayout addCopyForm = new FormLayout();
        DatePicker acquisitionDateField = new DatePicker("Date d'acquisition");
        acquisitionDateField.setValue(LocalDate.now());

        TextField priceField = new TextField("Prix");
        priceField.setPlaceholder("0.00");

        Button addCopyButton = new Button("Ajouter", e -> {
            if (acquisitionDateField.isEmpty() || priceField.isEmpty()) {
                Notification.show("Veuillez remplir tous les champs",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            try {
                double price = Double.parseDouble(priceField.getValue().replace(',', '.'));

                CopyDto newCopy = new CopyDto();
                newCopy.setItem(item);
                newCopy.setAcquisitionDate(acquisitionDateField.getValue());
                newCopy.setPrice(price);
                newCopy.setStatus("available");

                copyService.save(newCopy);

                // Clear form
                acquisitionDateField.setValue(LocalDate.now());
                priceField.clear();

                // Refresh grid
                refreshCopiesGrid(copiesGrid, item);

                Notification.show("Exemplaire ajouté avec succès",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Also refresh the main results grid to update copy count
                updateResults();

            } catch (NumberFormatException ex) {
                Notification.show("Prix invalide",
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        addCopyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        addCopyForm.add(acquisitionDateField, priceField, addCopyButton);

        Button closeButton = new Button("Fermer", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Layout organization
        dialogLayout.add(title, copiesGrid, addCopyTitle, addCopyForm, closeButton);
        dialogLayout.setFlexGrow(1, copiesGrid);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void refreshCopiesGrid(Grid<CopyDto> grid, ItemDto item) {
        List<CopyDto> copies = copyService.findByItem(item.getId());
        grid.setItems(copies);
    }

    private void changeCopyStatus(CopyDto copy, String newStatus) {
        copy.setStatus(newStatus);
        try {
            copyService.save(copy);

            String statusLabel = translateStatus(newStatus);
            Notification.show("Statut de l'exemplaire changé à \"" + statusLabel + "\"",
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Erreur lors du changement de statut: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "available" -> "Disponible";
            case "unavailable" -> "Indisponible";
            case "borrowed" -> "Emprunté";
            case "reserved" -> "Réservé";
            case "deleted" -> "Supprimé";
            default -> status;
        };
    }

    private void confirmDeleteItem(ItemDto item) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer la suppression");
        dialog.setText("Êtes-vous sûr de vouloir supprimer le document \"" + item.getTitle()
                + "\" ? Cette action marquera tous ses exemplaires comme supprimés.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteItem(item));

        dialog.open();
    }

    private void deleteItem(ItemDto item) {
        // Mark all copies as deleted instead of removing them
        List<CopyDto> copies = copyService.findByItem(item.getId());
        boolean success = true;

        for (CopyDto copy : copies) {
            if ("borrowed".equals(copy.getStatus()) || "reserved".equals(copy.getStatus())) {
                Notification.show(
                        "Impossible de supprimer un document avec des exemplaires empruntés ou réservés",
                        5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            copy.setStatus("deleted");
            try {
                copyService.save(copy);
            } catch (Exception e) {
                success = false;
            }
        }

        if (success) {
            Notification.show(
                    "Document et ses exemplaires marqués comme supprimés",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Refresh grid
            updateResults();
        } else {
            Notification.show(
                    "Erreur lors de la suppression de certains exemplaires",
                    5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
}