package com.example.application.views.myview;

import com.example.application.entity.*;
import com.example.application.service.implementation.ItemServiceImpl;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Catalogue")
@Route(value = "/catalogue", layout = MainLayout.class)
@AnonymousAllowed
public class CatalogueView extends VerticalLayout {
    private ComboBox<String> typeComboBox;
    private FormLayout searchFieldsLayout;

    private Button searchButton;
    private Button hideSearchButton;
    private Boolean searchFieldsVisible = true;

    private Grid<Item> resultsGrid;
    private CallbackDataProvider<Item, Void> dataProvider;

    private ItemServiceImpl itemService;

    // Critères de recherche courants
    private String selectedType;
    private Map<String, Object> searchCriteria = new HashMap<>();


    // Champs de recherche pour les différents types d'items
    private TextField titleField ;
    private TextField authorField ;
    private TextField isbnField ;
    private DatePicker publicationDateField ;
    private ComboBox<Category> categoryComboBox ;
    private ComboBox<Publisher> publisherComboBox ;

    private TextField keywordField;

    private TextField isniField ;
    private ComboBox<String> monthComboBox ;

    private IntegerField numberOfPiecesField ;
    private IntegerField recommendedAgeField ;


    public CatalogueView(@Autowired ItemServiceImpl itemService) {
        this.itemService = itemService;
        setWidth("100%");
        getStyle().set("flex-grow", "1");
        setHeight("100%");
        configureComponents();
        //add(createSearchLayout());
    }

    private void configureComponents() {
        // Initialisation des composants
        typeComboBox = new ComboBox<>("Type d'item");
        typeComboBox.setItems("Tous", "Livre", "Revue", "Jeu");
        typeComboBox.setValue("Tous");

        searchFieldsLayout = new FormLayout();
        searchFieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2), new FormLayout.ResponsiveStep("500px", 3));

        searchButton = new Button("Rechercher", e -> searchItems());

        hideSearchButton = new Button("Masquer", e -> {
            searchFieldsLayout.setVisible(!searchFieldsVisible);
            searchFieldsVisible = !searchFieldsVisible;
            hideSearchButton.setText((searchFieldsVisible ? "Masquer" : "Afficher"));
        });

        searchButton.setWidth("60%");
        hideSearchButton.setWidth("60%");

        add(createSearchLayout());
        configureGrid();
    }

    private Component createSearchLayout() {
        VerticalLayout layout = new VerticalLayout();

        // Listener pour adapter les inputs en fonction du type
        typeComboBox.addValueChangeListener(e -> updateSearchFields());

        // Initialisation des champs de recherche
        updateSearchFields();

        HorizontalLayout buttonsLayout = new HorizontalLayout(searchButton, hideSearchButton);

        layout.add(typeComboBox, searchFieldsLayout, buttonsLayout);
        return layout;
    }

    private void updateCategoryComboBox() {
        List<Category> categories = itemService.getAllCategories();
        ListDataProvider<Category> dataProviderCategory = new ListDataProvider<>(categories);
        categoryComboBox.setItemLabelGenerator(Category::getName);
        categoryComboBox.setItems(dataProviderCategory);
    }

    private void updatePublisherComboBox() {
        List<Publisher> publishers = itemService.getAllPublishers();
        ListDataProvider<Publisher> dataProviderPublisher = new ListDataProvider<>(publishers);
        publisherComboBox.setItemLabelGenerator(Publisher::getName);
        publisherComboBox.setItems(dataProviderPublisher);

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

        this.titleField = new TextField("Titre");
        this.authorField = new TextField("Auteur");
        this.isbnField = new TextField("ISBN");
        this.publicationDateField = new DatePicker("Date de Publication");
        this.categoryComboBox = new ComboBox<>("Catégorie");

        updateCategoryComboBox();

        this.publisherComboBox = new ComboBox<>("Éditeur");
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, authorField, isbnField, publicationDateField, categoryComboBox, publisherComboBox);
    }

    private void createMagazineSearchFields() {
        searchCriteria.clear();

        this.titleField = new TextField("Titre");
        this.isniField = new TextField("ISNI");
        this.monthComboBox = new ComboBox<>("Mois de Publication");

        monthComboBox.setItems(Arrays.stream(Month.values())
                .map(month -> month.getDisplayName(TextStyle.FULL, Locale.FRENCH))
                .collect(Collectors.toList())); //TODO voir solutions

        this.publicationDateField = new DatePicker("Date de Publication");
        this.categoryComboBox = new ComboBox<>("Catégorie");

        updateCategoryComboBox();

        this.publisherComboBox = new ComboBox<>("Éditeur");
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, isniField, monthComboBox, publicationDateField, categoryComboBox, publisherComboBox);
    }

    private void createGameSearchFields() {
        searchCriteria.clear();

        this.titleField = new TextField("Titre");
        this.numberOfPiecesField = new IntegerField("Nombre de Pièces");
        this.recommendedAgeField = new IntegerField("Âge Recommandé");
        this.categoryComboBox = new ComboBox<>("Catégorie");
        updateCategoryComboBox();
        this.publisherComboBox = new ComboBox<>("Éditeur");
        updatePublisherComboBox();

        searchFieldsLayout.add(titleField, numberOfPiecesField, recommendedAgeField, categoryComboBox, publisherComboBox);
    }

    private void createGeneralSearchFields() {
        searchCriteria.clear();

        this.keywordField = new TextField("Mot-clé");
        this.categoryComboBox = new ComboBox<>("Catégorie");
        updateCategoryComboBox();
        this.publisherComboBox = new ComboBox<>("Éditeur");
        updatePublisherComboBox();

        searchFieldsLayout.add(keywordField, categoryComboBox, publisherComboBox);
    }

    private void searchItems() {
        selectedType = typeComboBox.getValue();
        searchCriteria.clear();

        if ("Livre".equals(selectedType)) {
            TextField titleField = this.titleField;
            TextField authorField = this.authorField;
            TextField isbnField = this.isbnField;
            DatePicker publicationDateField = this.publicationDateField;
            ComboBox<Category> categoryComboBox = this.categoryComboBox;
            ComboBox<Publisher> publisherComboBox = this.publisherComboBox;

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("author", authorField.getValue());
            searchCriteria.put("isbn", isbnField.getValue());
            searchCriteria.put("publicationDate", publicationDateField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else if ("Revue".equals(selectedType)) {
            TextField titleField = this.titleField;
            TextField isniField = this.isniField;
            ComboBox<String> monthComboBox = this.monthComboBox;
            DatePicker publicationDateField = this.publicationDateField;
            ComboBox<Category> categoryComboBox = this.categoryComboBox;
            ComboBox<Publisher> publisherComboBox = this.publisherComboBox;

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("isni", isniField.getValue());
            searchCriteria.put("month", monthComboBox.getValue());
            searchCriteria.put("publicationDate", publicationDateField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else if ("Jeu".equals(selectedType)) {
            TextField titleField = this.titleField;
            IntegerField numberOfPiecesField = this.numberOfPiecesField;
            IntegerField recommendedAgeField = this.recommendedAgeField;
            ComboBox<Category> categoryComboBox = this.categoryComboBox;
            ComboBox<Publisher> publisherComboBox = this.publisherComboBox;

            searchCriteria.put("title", titleField.getValue());
            searchCriteria.put("numberOfPieces", numberOfPiecesField.getValue());
            searchCriteria.put("recommendedAge", recommendedAgeField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());

        } else {
            TextField keywordField = this.keywordField;
            ComboBox<Category> categoryComboBox = this.categoryComboBox;
            ComboBox<Publisher> publisherComboBox = this.publisherComboBox;

            searchCriteria.put("keyword", keywordField.getValue());
            searchCriteria.put("category", categoryComboBox.getValue());
            searchCriteria.put("publisher", publisherComboBox.getValue());
        }

        resultsGrid.setItems(query -> {
            int offset = query.getOffset();
            int limit = query.getLimit();
            return itemService.fetchItemsWithFilters(searchCriteria, selectedType, offset, limit).stream();
        });
    }

    private String translateType(String type) {
        switch (type) {
            case "book":
                return "Livre";
            case "magazine":
                return "Revue";
            case "board_game":
                return "Jeu";
            default:
                return type;
        }
    }

    private void configureGrid() {
        resultsGrid = new Grid<>(Item.class);
        resultsGrid.setSizeFull();

        resultsGrid.removeAllColumns();

        resultsGrid.addColumn(Item::getTitle).setHeader("Titre").setSortable(true).setSortProperty("title");
        resultsGrid.addColumn(item -> translateType(item.getType())).setHeader("Type").setSortable(true);
        resultsGrid.addColumn(item -> item.getCategory().getName()).setHeader("Catégorie");
        resultsGrid.addColumn(item -> item.getPublisher().getName()).setHeader("Éditeur");

        // Colonnes spécifiques en fonction du type
        /*resultsGrid.addColumn(item -> {
            if ("Livre".equals(item.getType())) {
                Book book = itemService.findBookByItemId(item.getId());
                return book != null ? book.getAuthor() : "";
            } else if ("Revue".equals(item.getType())) {
                Magazine magazine = itemService.findMagazineByItemId(item.getId());
                return magazine != null ? magazine.getIsni() : "";
            } else if ("Jeu".equals(item.getType())) {
                BoardGame game = itemService.findBoardGameByItemId(item.getId());
                return game != null ? game.getRecommendedAge() : "";
            } else {
                return "";
            }
        }).setHeader("Détails");*/

        resultsGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        // Ajouter un listener pour la sélection des items
        resultsGrid.asSingleSelect().addValueChangeListener(event -> {
            Item selectedItem = event.getValue();
            if (selectedItem != null) {
                resultsGrid.getUI().ifPresent(ui ->
                        ui.navigate(ItemDetailView.class, selectedItem.getId())
                );
            }
        });

        // Configuration du DataProvider
        resultsGrid.setPageSize(20);
        resultsGrid.setItems(query -> {
            int offset = query.getOffset();
            int limit = query.getLimit();
            return itemService.fetchItemsWithFilters(searchCriteria, selectedType, offset, limit).stream();
        });

        //resultsGrid.setDataProvider(dataProvider);

        add(resultsGrid);
    }
}
