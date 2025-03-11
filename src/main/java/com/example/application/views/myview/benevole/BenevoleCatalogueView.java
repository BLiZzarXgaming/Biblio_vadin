package com.example.application.views.myview.benevole;

import com.example.application.entity.DTO.*;
import com.example.application.objectcustom.MoisOption;
import com.example.application.service.implementation.*;
import com.example.application.utils.StatusUtils;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@PageTitle("Gestion du Catalogue")
@Route(value = "volunteer/catalogue", layout = MainLayout.class)
@RolesAllowed("ROLE_BÉNÉVOLE")
public class BenevoleCatalogueView extends VerticalLayout {

    private final ItemServiceV2 itemService;
    private final CategoryServiceV2 categoryService;
    private final PublisherServiceV2 publisherService;
    private final BookServiceV2 bookService;
    private final MagazineServiceV2 magazineService;
    private final BoardGameServiceV2 boardGameService;
    private final CopyServiceV2 copyService;

    // Composants UI
    private ComboBox<String> typeFilter;
    private ComboBox<CategoryDto> categoryFilter;
    private TextField titleFilter;
    private TextField authorFilter;
    private TextField isbnFilter;
    private TextField isniFilter;
    private TextField gtinFilter;
    private ComboBox<PublisherDto> publisherFilter;
    private Button searchButton;
    private Button resetButton;

    // Grille des résultats
    private Grid<ItemDto> resultsGrid;

    // Pagination
    private int currentPage = 0;
    private int pageSize = 20;
    private Span paginationInfo;
    private Button prevPageButton;
    private Button nextPageButton;
    private IntegerField pageField;

    // État de la recherche
    private Map<String, Object> searchCriteria = new HashMap<>();
    private String selectedType = "Tous";
    private long totalItems = 0;

    public BenevoleCatalogueView(
            ItemServiceV2 itemService,
            CategoryServiceV2 categoryService,
            PublisherServiceV2 publisherService,
            BookServiceV2 bookService,
            MagazineServiceV2 magazineService,
            BoardGameServiceV2 boardGameService,
            CopyServiceV2 copyService) {

        this.itemService = itemService;
        this.categoryService = categoryService;
        this.publisherService = publisherService;
        this.bookService = bookService;
        this.magazineService = magazineService;
        this.boardGameService = boardGameService;
        this.copyService = copyService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Gestion du Catalogue");
        add(title);

        // Création des sections
        createFilterSection();
        createResultsGrid();
        createPaginationControls();

        // Chargement initial
        searchItems();
    }

    private void createFilterSection() {
        // Titre de la section
        H3 sectionTitle = new H3("Recherche de documents");

        // Type de document
        typeFilter = new ComboBox<>("Type");
        typeFilter.setItems("Tous",
                StatusUtils.DocTypes.toFrench(StatusUtils.DocTypes.BOOK),
                StatusUtils.DocTypes.toFrench(StatusUtils.DocTypes.MAGAZINE),
                StatusUtils.DocTypes.toFrench(StatusUtils.DocTypes.BOARD_GAME));
        typeFilter.setValue("Tous");
        typeFilter.addValueChangeListener(e -> updateFiltersByType(e.getValue()));

        // Catégorie
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(categoryService.findAll());
        categoryFilter.setItemLabelGenerator(CategoryDto::getName);

        // Éditeur
        publisherFilter = new ComboBox<>("Éditeur");
        publisherFilter.setItems(publisherService.findAll());
        publisherFilter.setItemLabelGenerator(PublisherDto::getName);

        // Titre
        titleFilter = new TextField("Titre");
        titleFilter.setValueChangeMode(ValueChangeMode.EAGER);

        // Filtres spécifiques (initialement cachés)
        authorFilter = new TextField("Auteur");
        isbnFilter = new TextField("ISBN");
        isniFilter = new TextField("ISNI");
        gtinFilter = new TextField("GTIN");

        // Boutons
        searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchItems());

        resetButton = new Button("Réinitialiser");
        resetButton.addClickListener(e -> resetFilters());

        // Layout pour les filtres de base
        FormLayout baseFilterLayout = new FormLayout();
        baseFilterLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3));
        baseFilterLayout.add(typeFilter, categoryFilter, publisherFilter, titleFilter);

        // Layout pour les filtres spécifiques
        FormLayout specificFilterLayout = new FormLayout();
        specificFilterLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3));
        specificFilterLayout.add(authorFilter, isbnFilter, isniFilter, gtinFilter);

        // Initialiser les filtres visibles en fonction du type par défaut
        updateFiltersByType("Tous");

        // Layout pour les boutons
        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton, resetButton);
        buttonLayout.setSpacing(true);

        // Layout principal
        VerticalLayout filterSection = new VerticalLayout(
                sectionTitle,
                baseFilterLayout,
                specificFilterLayout,
                buttonLayout);
        filterSection.setPadding(false);
        filterSection.setSpacing(true);

        add(filterSection);
    }

    private void updateFiltersByType(String type) {
        boolean isBook = StatusUtils.DocTypes.LIVRE.equals(type);
        boolean isMagazine = StatusUtils.DocTypes.REVUE.equals(type);
        boolean isGame = StatusUtils.DocTypes.JEU.equals(type);

        // Cacher tous les filtres spécifiques
        authorFilter.setVisible(false);
        isbnFilter.setVisible(false);
        isniFilter.setVisible(false);
        gtinFilter.setVisible(false);

        // Afficher les filtres selon le type sélectionné
        if (isBook) {
            authorFilter.setVisible(true);
            isbnFilter.setVisible(true);
        } else if (isMagazine) {
            isniFilter.setVisible(true);
        } else if (isGame) {
            gtinFilter.setVisible(true);
        }
    }

    private void createResultsGrid() {
        resultsGrid = new Grid<>();

        // Configuration des colonnes
        resultsGrid.addColumn(ItemDto::getTitle).setHeader("Titre").setAutoWidth(true).setFlexGrow(1);

        resultsGrid.addColumn(item -> {
            return StatusUtils.DocTypes.toFrench(item.getType());
        }).setHeader("Type").setAutoWidth(true);

        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie").setAutoWidth(true);
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur").setAutoWidth(true);

        // Colonne pour les actions
        resultsGrid.addColumn(new ComponentRenderer<>(item -> {
            HorizontalLayout layout = new HorizontalLayout();

            // Bouton détails
            Button detailsButton = new Button(new Icon(VaadinIcon.SEARCH));
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            detailsButton.addClickListener(e -> showItemDetails(item));
            detailsButton.getElement().setAttribute("title", "Voir les détails");

            // Bouton édition
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> editItem(item));
            editButton.getElement().setAttribute("title", "Modifier");

            // Bouton gestion des copies
            Button copiesButton = new Button(new Icon(VaadinIcon.COPY));
            copiesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            copiesButton.addClickListener(e -> manageCopies(item));
            copiesButton.getElement().setAttribute("title", "Gérer les copies");

            // Bouton marquer comme supprimé
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY,
                    ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteItem(item));
            deleteButton.getElement().setAttribute("title", "Marquer comme supprimé");

            layout.add(detailsButton, editButton, copiesButton, deleteButton);
            return layout;
        })).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        resultsGrid.setHeight("500px");

        add(resultsGrid);
    }

    private void createPaginationControls() {
        HorizontalLayout paginationLayout = new HorizontalLayout();
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        paginationLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        prevPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        prevPageButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePagination();
                searchItems();
            }
        });

        pageField = new IntegerField();
        pageField.setValue(currentPage + 1);
        // pageField.setHasControls(false);
        pageField.setWidth("60px");
        pageField.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() > 0) {
                currentPage = e.getValue() - 1;
                updatePagination();
                searchItems();
            }
        });

        nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        nextPageButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextPageButton.addClickListener(e -> {
            int maxPage = (int) Math.ceil((double) totalItems / pageSize) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                updatePagination();
                searchItems();
            }
        });

        paginationInfo = new Span();

        paginationLayout.add(prevPageButton, pageField, nextPageButton, paginationInfo);
        add(paginationLayout);
    }

    private void updatePagination() {
        pageField.setValue(currentPage + 1);

        int startItem = currentPage * pageSize + 1;
        int endItem = Math.min((currentPage + 1) * pageSize, (int) totalItems);
        int maxPage = (int) Math.ceil((double) totalItems / pageSize);

        paginationInfo.setText(String.format(" Page %d / %d - Éléments %d à %d sur %d",
                currentPage + 1, Math.max(1, maxPage),
                Math.min(startItem, (int) totalItems),
                endItem, (int) totalItems));

        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < maxPage - 1);
    }

    private void searchItems() {
        // Préparation des critères de recherche
        searchCriteria.clear();
        selectedType = typeFilter.getValue();

        if (categoryFilter.getValue() != null) {
            searchCriteria.put("category", categoryFilter.getValue());
        }

        if (publisherFilter.getValue() != null) {
            searchCriteria.put("publisher", publisherFilter.getValue());
        }

        if (!titleFilter.getValue().isEmpty()) {
            searchCriteria.put("keyword", titleFilter.getValue());
        }

        // Critères spécifiques selon le type
        if (StatusUtils.DocTypes.LIVRE.equals(selectedType)) {
            if (!authorFilter.getValue().isEmpty()) {
                searchCriteria.put("author", authorFilter.getValue());
            }
            if (!isbnFilter.getValue().isEmpty()) {
                searchCriteria.put("isbn", isbnFilter.getValue());
            }
        } else if (StatusUtils.DocTypes.REVUE.equals(selectedType)) {
            if (!isniFilter.getValue().isEmpty()) {
                searchCriteria.put("isni", isniFilter.getValue());
            }
        } else if (StatusUtils.DocTypes.JEU.equals(selectedType)) {
            if (!gtinFilter.getValue().isEmpty()) {
                searchCriteria.put("gtin", gtinFilter.getValue());
            }
        }

        // Mise à jour du total d'items
        totalItems = countItemsWithFilters();

        // Mise à jour de la pagination
        updatePagination();

        // Récupération des données avec pagination
        List<ItemDto> items = fetchItemsWithPagination();
        resultsGrid.setItems(items);

        if (items.isEmpty()) {
            showNotification("Aucun document trouvé", "info");
        }
    }

    private List<ItemDto> fetchItemsWithPagination() {
        return itemService.fetchItemsWithFilters(
                searchCriteria,
                selectedType,
                currentPage * pageSize,
                pageSize);
    }

    //TODO : Implementer la methode pour de vrai
    private long countItemsWithFilters() {
        // Dans une implémentation réelle, cette méthode appellerait un service
        // qui compterait les items répondant aux critères sans les charger tous.
        // Pour cet exemple, nous allons charger tous les items pour obtenir le compte.
        // Cela fonctionnera pour de petits ensembles de données mais n'est pas efficace
        // pour de grandes bases de données.

        // Simuler le compte total (dans une implémentation réelle, ce serait un appel à
        // la base de données)
        List<ItemDto> allMatchingItems = itemService.fetchItemsWithFilters(
                searchCriteria,
                selectedType,
                0,
                Integer.MAX_VALUE);

        return allMatchingItems.size();
    }

    private void resetFilters() {
        typeFilter.setValue("Tous");
        categoryFilter.clear();
        publisherFilter.clear();
        titleFilter.clear();
        authorFilter.clear();
        isbnFilter.clear();
        isniFilter.clear();
        gtinFilter.clear();

        updateFiltersByType("Tous");
        currentPage = 0;
        searchItems();
    }

    private void showItemDetails(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H2 title = new H2(item.getTitle());

        // Informations générales
        FormLayout infoLayout = new FormLayout();
        infoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        TextField typeField = createReadOnlyTextField("Type", StatusUtils.DocTypes.toFrench(item.getType()));
        TextField categoryField = createReadOnlyTextField("Catégorie", item.getCategory().getName());
        TextField publisherField = createReadOnlyTextField("Éditeur", item.getPublisher().getName());
        TextField valueField = createReadOnlyTextField("Valeur", item.getValue().toString() + " $");

        infoLayout.add(typeField, categoryField, publisherField, valueField);

        // Informations spécifiques selon le type
        VerticalLayout specificLayout = new VerticalLayout();
        specificLayout.setPadding(false);
        specificLayout.setSpacing(true);

        H3 specificTitle = new H3("Informations spécifiques");
        specificLayout.add(specificTitle);

        FormLayout specificInfoLayout = new FormLayout();
        specificInfoLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        if (StatusUtils.DocTypes.BOOK.equals(item.getType())) {
            Optional<BookDto> book = bookService.findById(item.getId());
            if (book.isPresent()) {
                specificInfoLayout.add(createReadOnlyTextField("Auteur", book.get().getAuthor()));
                specificInfoLayout.add(createReadOnlyTextField("ISBN", book.get().getIsbn()));
                specificInfoLayout.add(createReadOnlyTextField("Date de publication",
                        book.get().getPublicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        } else if (StatusUtils.DocTypes.MAGAZINE.equals(item.getType())) {
            Optional<MagazineDto> magazine = magazineService.findById(item.getId());
            if (magazine.isPresent()) {
                specificInfoLayout.add(createReadOnlyTextField("ISNI", magazine.get().getIsni()));
                specificInfoLayout.add(createReadOnlyTextField("Mois", magazine.get().getMonth()));
                specificInfoLayout.add(createReadOnlyTextField("Année", magazine.get().getYear()));
                specificInfoLayout.add(createReadOnlyTextField("Date de publication",
                        magazine.get().getPublicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        } else if (StatusUtils.DocTypes.BOARD_GAME.equals(item.getType())) {
            Optional<BoardGameDto> boardGame = boardGameService.findById(item.getId());
            if (boardGame.isPresent()) {
                specificInfoLayout.add(createReadOnlyTextField("Nombre de pièces",
                        boardGame.get().getNumberOfPieces().toString()));
                specificInfoLayout.add(createReadOnlyTextField("Âge recommandé",
                        boardGame.get().getRecommendedAge().toString() + " ans"));
                specificInfoLayout.add(createReadOnlyTextField("GTIN", boardGame.get().getGtin()));

                TextArea rulesArea = new TextArea("Règles du jeu");
                rulesArea.setValue(boardGame.get().getGameRules());
                rulesArea.setReadOnly(true);
                rulesArea.setWidthFull();
                rulesArea.setHeight("200px");
                specificInfoLayout.add(rulesArea);
            }
        }

        specificLayout.add(specificInfoLayout);

        // Lien externe
        VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setPadding(false);
        linkLayout.setSpacing(true);

        if (item.getLink() != null && !item.getLink().isEmpty()) {
            H3 linkTitle = new H3("Lien externe");
            Anchor link = new Anchor(item.getLink(), "Voir plus d'informations");
            link.setTarget("_blank");
            linkLayout.add(linkTitle, link);
        }

        // Bouton pour fermer
        Button closeButton = new Button("Fermer");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addClickListener(e -> dialog.close());

        // Ajouter tous les éléments au layout
        layout.add(title, infoLayout, specificLayout, linkLayout, closeButton);

        dialog.add(layout);
        dialog.open();
    }

    private TextField createReadOnlyTextField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        return field;
    }

    private void editItem(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H2 title = new H2("Modifier " + item.getTitle());

        // Formulaire général
        FormLayout generalForm = new FormLayout();
        generalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        TextField titleField = new TextField("Titre");
        titleField.setValue(item.getTitle());
        titleField.setRequired(true);

        ComboBox<CategoryDto> categoryField = new ComboBox<>("Catégorie");
        categoryField.setItems(categoryService.findAll());
        categoryField.setItemLabelGenerator(CategoryDto::getName);
        categoryField.setValue(item.getCategory());
        categoryField.setRequired(true);

        ComboBox<PublisherDto> publisherField = new ComboBox<>("Éditeur");
        publisherField.setItems(publisherService.findAll());
        publisherField.setItemLabelGenerator(PublisherDto::getName);
        publisherField.setValue(item.getPublisher());
        publisherField.setRequired(true);

        TextField valueField = new TextField("Valeur");
        valueField.setValue(item.getValue().toString());
        valueField.setRequired(true);

        TextField linkField = new TextField("Lien externe");
        linkField.setValue(item.getLink() != null ? item.getLink() : "");

        generalForm.add(titleField, categoryField, publisherField, valueField, linkField);

        // Formulaire spécifique selon le type
        FormLayout specificForm = new FormLayout();
        specificForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2));

        // Variables pour stocker les champs spécifiques
        TextField authorField = null;
        TextField isbnField = null;
        DatePicker publicationDateField = null;
        TextField isniField = null;
        ComboBox<MoisOption> monthField = null;
        TextField yearField = null;
        IntegerField numberOfPiecesField = null;
        IntegerField recommendedAgeField = null;
        TextField gtinField = null;
        TextArea gameRulesArea = null;

        H3 specificTitle = new H3("Informations spécifiques");

        if (StatusUtils.DocTypes.BOOK.equals(item.getType())) {
            Optional<BookDto> book = bookService.findById(item.getId());
            if (book.isPresent()) {
                authorField = new TextField("Auteur");
                authorField.setValue(book.get().getAuthor());
                authorField.setRequired(true);

                isbnField = new TextField("ISBN");
                isbnField.setValue(book.get().getIsbn());
                isbnField.setRequired(true);
                isbnField.setReadOnly(true); // ISBN est unique, ne peut pas être modifié

                publicationDateField = new DatePicker("Date de publication");
                publicationDateField.setValue(book.get().getPublicationDate());
                publicationDateField.setRequired(true);

                specificForm.add(authorField, isbnField, publicationDateField);
            }
        } else if (StatusUtils.DocTypes.MAGAZINE.equals(item.getType())) {
            Optional<MagazineDto> magazine = magazineService.findById(item.getId());
            if (magazine.isPresent()) {
                isniField = new TextField("ISNI");
                isniField.setValue(magazine.get().getIsni());
                isniField.setRequired(true);
                isniField.setReadOnly(true); // ISNI est unique, ne peut pas être modifié

                monthField = new ComboBox<>("Mois");
                monthField.setItems(MoisOption.getListeMois());
                monthField.setItemLabelGenerator(MoisOption::getNom);
                monthField.setValue(MoisOption.getListeMois().stream()
                        .filter(m -> m.getNumero().equals(magazine.get().getMonth()))
                        .findFirst().orElse(null));
                monthField.setReadOnly(true); // Mois est unique avec ISNI et année, ne peut pas être modifié

                yearField = new TextField("Année");
                yearField.setValue(magazine.get().getYear());
                yearField.setReadOnly(true); // Année est unique avec ISNI et mois, ne peut pas être modifié

                publicationDateField = new DatePicker("Date de publication");
                publicationDateField.setValue(magazine.get().getPublicationDate());
                publicationDateField.setRequired(true);

                specificForm.add(isniField, monthField, yearField, publicationDateField);
            }
        } else if (StatusUtils.DocTypes.BOARD_GAME.equals(item.getType())) {
            Optional<BoardGameDto> boardGame = boardGameService.findById(item.getId());
            if (boardGame.isPresent()) {
                numberOfPiecesField = new IntegerField("Nombre de pièces");
                numberOfPiecesField.setValue(boardGame.get().getNumberOfPieces());
                numberOfPiecesField.setMin(1);
                numberOfPiecesField.setRequired(true);

                recommendedAgeField = new IntegerField("Âge recommandé");
                recommendedAgeField.setValue(boardGame.get().getRecommendedAge());
                recommendedAgeField.setMin(1);
                recommendedAgeField.setRequired(true);

                gtinField = new TextField("GTIN");
                gtinField.setValue(boardGame.get().getGtin());
                gtinField.setRequired(true);
                gtinField.setReadOnly(true); // GTIN est unique, ne peut pas être modifié

                gameRulesArea = new TextArea("Règles du jeu");
                gameRulesArea.setValue(boardGame.get().getGameRules());
                gameRulesArea.setRequired(true);
                gameRulesArea.setWidthFull();
                gameRulesArea.setHeight("200px");

                specificForm.add(numberOfPiecesField, recommendedAgeField, gtinField);
                specificForm.add(gameRulesArea);
            }
        }

        // Boutons
        Button saveButton = new Button("Enregistrer");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final TextField finalAuthorField = authorField;
        final TextField finalIsbnField = isbnField;
        final DatePicker finalPublicationDateField = publicationDateField;
        final TextField finalIsniField = isniField;
        final ComboBox<MoisOption> finalMonthField = monthField;
        final TextField finalYearField = yearField;
        final IntegerField finalNumberOfPiecesField = numberOfPiecesField;
        final IntegerField finalRecommendedAgeField = recommendedAgeField;
        final TextField finalGtinField = gtinField;
        final TextArea finalGameRulesArea = gameRulesArea;

        saveButton.addClickListener(e -> {
            try {
                // Valider les champs
                if (titleField.isEmpty() || categoryField.isEmpty() || publisherField.isEmpty()
                        || valueField.isEmpty()) {
                    showNotification("Veuillez remplir tous les champs obligatoires", "error");
                    return;
                }

                // Mettre à jour l'item de base
                item.setTitle(titleField.getValue());
                item.setCategory(categoryField.getValue());
                item.setPublisher(publisherField.getValue());
                try {
                    item.setValue(Double.parseDouble(valueField.getValue()));
                } catch (NumberFormatException ex) {
                    showNotification("Valeur invalide", "error");
                    return;
                }
                item.setLink(linkField.getValue());

                // Mettre à jour l'entité spécifique
                if (StatusUtils.DocTypes.BOOK.equals(item.getType())) {
                    if (finalAuthorField.isEmpty() || finalPublicationDateField.isEmpty()) {
                        showNotification("Veuillez remplir tous les champs obligatoires", "error");
                        return;
                    }

                    BookDto book = bookService.findById(item.getId()).orElse(new BookDto());
                    book.setAuthor(finalAuthorField.getValue());
                    book.setPublicationDate(finalPublicationDateField.getValue());
                    book.setItem(item);
                    bookService.save(book);

                } else if (StatusUtils.DocTypes.MAGAZINE.equals(item.getType())) {
                    if (finalPublicationDateField.isEmpty()) {
                        showNotification("Veuillez remplir tous les champs obligatoires", "error");
                        return;
                    }

                    MagazineDto magazine = magazineService.findById(item.getId()).orElse(new MagazineDto());
                    magazine.setPublicationDate(finalPublicationDateField.getValue());
                    magazine.setItem(item);
                    magazineService.save(magazine);

                } else if (StatusUtils.DocTypes.BOARD_GAME.equals(item.getType())) {
                    if (finalNumberOfPiecesField.isEmpty() || finalRecommendedAgeField.isEmpty()
                            || finalGameRulesArea.isEmpty()) {
                        showNotification("Veuillez remplir tous les champs obligatoires", "error");
                        return;
                    }

                    BoardGameDto boardGame = boardGameService.findById(item.getId()).orElse(new BoardGameDto());
                    boardGame.setNumberOfPieces(finalNumberOfPiecesField.getValue());
                    boardGame.setRecommendedAge(finalRecommendedAgeField.getValue());
                    boardGame.setGameRules(finalGameRulesArea.getValue());
                    boardGame.setItem(item);
                    boardGameService.save(boardGame);
                }

                // Sauvegarder l'item
                itemService.save(item);

                showNotification("Document mis à jour avec succès", "success");
                dialog.close();
                searchItems(); // Rafraîchir la grille

            } catch (Exception ex) {
                showNotification("Erreur lors de la mise à jour: " + ex.getMessage(), "error");
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        // Ajouter tous les éléments au layout
        layout.add(title, generalForm, specificTitle, specificForm, buttonLayout);

        dialog.add(layout);
        dialog.open();
    }

    private void manageCopies(ItemDto item) {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H2 title = new H2("Gestion des copies de " + item.getTitle());

        // Grille des copies
        Grid<CopyDto> copiesGrid = new Grid<>();

        copiesGrid.addColumn(CopyDto::getId).setHeader("ID").setAutoWidth(true);

        copiesGrid.addColumn(copy -> {
            return StatusUtils.ItemStatus.toFrench(copy.getStatus());
        }).setHeader("Statut").setAutoWidth(true);

        copiesGrid.addColumn(copy -> copy.getAcquisitionDate() != null
                ? copy.getAcquisitionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "").setHeader("Date d'acquisition").setAutoWidth(true);

        copiesGrid.addColumn(CopyDto::getPrice).setHeader("Prix").setAutoWidth(true);

        copiesGrid.addColumn(new ComponentRenderer<>(copy -> {
            HorizontalLayout actionLayout = new HorizontalLayout();

            if (StatusUtils.ItemStatus.AVAILABLE.equals(copy.getStatus())) {
                Button unavailableButton = new Button("Indisponible");
                unavailableButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                unavailableButton.addClickListener(e -> setItemStatus(copy, StatusUtils.ItemStatus.UNAVAILABLE, copiesGrid, item));
                actionLayout.add(unavailableButton);

                Button deleteButton = new Button("Supprimer");
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                deleteButton.addClickListener(e -> setItemStatus(copy, StatusUtils.ItemStatus.DELETED, copiesGrid, item));
                actionLayout.add(deleteButton);
            } else if (StatusUtils.ItemStatus.UNAVAILABLE.equals(copy.getStatus()) || StatusUtils.ItemStatus.DELETED.equals(copy.getStatus())) {
                Button availableButton = new Button("Rendre disponible");
                availableButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                availableButton.addClickListener(e -> setItemStatus(copy, StatusUtils.ItemStatus.AVAILABLE, copiesGrid, item));
                actionLayout.add(availableButton);
            }

            return actionLayout;
        })).setHeader("Actions").setAutoWidth(true);

        copiesGrid.setHeight("400px");

        // Charger les copies
        List<CopyDto> copies = copyService.findByItem(item.getId());
        copiesGrid.setItems(copies);

        // Boutton ajouter une copie
        Button addCopyButton = new Button("Ajouter une copie");
        addCopyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCopyButton.addClickListener(e -> addNewCopy(item, copiesGrid));

        // Bouton pour fermer
        Button closeButton = new Button("Fermer");
        closeButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(addCopyButton, closeButton);
        buttonLayout.setSpacing(true);

        layout.add(title, copiesGrid, buttonLayout);

        dialog.add(layout);
        dialog.open();
    }

    private void addNewCopy(ItemDto item, Grid<CopyDto> copiesGrid) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Ajouter une nouvelle copie");

        FormLayout formLayout = new FormLayout();

        DatePicker acquisitionDateField = new DatePicker("Date d'acquisition");
        acquisitionDateField.setValue(LocalDate.now());
        acquisitionDateField.setRequired(true);

        TextField priceField = new TextField("Prix");
        priceField.setRequired(true);

        formLayout.add(acquisitionDateField, priceField);

        Button saveButton = new Button("Ajouter");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                if (acquisitionDateField.isEmpty() || priceField.isEmpty()) {
                    showNotification("Veuillez remplir tous les champs", "error");
                    return;
                }

                double price;
                try {
                    price = Double.parseDouble(priceField.getValue());
                } catch (NumberFormatException ex) {
                    showNotification("Prix invalide", "error");
                    return;
                }

                CopyDto newCopy = new CopyDto();
                newCopy.setItem(item);
                newCopy.setAcquisitionDate(acquisitionDateField.getValue());
                newCopy.setPrice(price);
                newCopy.setStatus(StatusUtils.ItemStatus.AVAILABLE);

                copyService.save(newCopy);

                showNotification("Copie ajoutée avec succès", "success");
                dialog.close();

                // Rafraîchir la grille des copies
                List<CopyDto> copies = copyService.findByItem(item.getId());
                copiesGrid.setItems(copies);

            } catch (Exception ex) {
                showNotification("Erreur lors de l'ajout de la copie: " + ex.getMessage(), "error");
            }
        });

        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.add(title, formLayout, buttonLayout);

        dialog.add(layout);
        dialog.open();
    }

    private void setItemStatus(CopyDto copy, String newStatus, Grid<CopyDto> copiesGrid, ItemDto item) {
        try {
            // Vérifier si la copie peut changer de statut
            if (StatusUtils.ItemStatus.BORROWED.equals(copy.getStatus()) || StatusUtils.ItemStatus.RESERVED.equals(copy.getStatus())) {
                showNotification("Impossible de modifier le statut d'une copie empruntée ou réservée", "error");
                return;
            }

            // Mettre à jour le statut
            copy.setStatus(newStatus);
            copyService.save(copy);

            // Message de confirmation
            String statusMessage = StatusUtils.ItemStatus.toFrench(newStatus);

            showNotification("Copie marquée comme " + statusMessage, "success");

            // Rafraîchir la grille des copies
            List<CopyDto> copies = copyService.findByItem(item.getId());
            copiesGrid.setItems(copies);

        } catch (Exception e) {
            showNotification("Erreur lors de la mise à jour du statut: " + e.getMessage(), "error");
        }
    }

    private void confirmDeleteItem(ItemDto item) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmation de suppression");
        dialog.setText("Êtes-vous sûr de vouloir marquer ce document comme supprimé ? " +
                "Cette action rendra toutes les copies indisponibles.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            try {
                // Marquer toutes les copies comme supprimées
                List<CopyDto> copies = copyService.findByItem(item.getId());
                for (CopyDto copy : copies) {
                    if (!StatusUtils.ItemStatus.BORROWED.equals(copy.getStatus()) && !StatusUtils.ItemStatus.RESERVED.equals(copy.getStatus())) {
                        copy.setStatus("deleted");
                        copyService.save(copy);
                    }
                }

                showNotification("Document et ses copies disponibles marqués comme supprimés", "success");
                searchItems(); // Rafraîchir la grille

            } catch (Exception e) {
                showNotification("Erreur lors de la suppression: " + e.getMessage(), "error");
            }
        });

        dialog.open();
    }

    private void showNotification(String message, String type) {
        Notification notification = Notification.show(message);
        notification.setDuration(3000);

        switch (type) {
            case "success" -> notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            case "error" -> notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            case "warning" -> notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
            case "info" -> notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }
    }
}